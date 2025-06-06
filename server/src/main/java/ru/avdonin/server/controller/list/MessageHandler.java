package ru.avdonin.server.controller.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.avdonin.server.service.list.ChatService;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.chat.dto.ChatIdDto;
import ru.avdonin.template.model.user.dto.UserDto;
import ru.avdonin.template.model.util.ResponseMessage;
import ru.avdonin.server.service.list.MessageService;
import ru.avdonin.server.service.list.UserService;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageHandler extends TextWebSocketHandler {
    private final Logger log;
    private final UserService userService;
    private final MessageService messageService;
    private final ChatService chatService;

    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String username = getUsernameFromSession(session);
            userService.searchUserByUsername(username, "EN");
            sessions.put(username, session);
            log.info("connection established, username: " + username);
        } catch (Exception e) {
            sendError(session, e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            String username = getUsernameFromSession(session);
            sessions.remove(username);
            log.info("connection closed");

        } catch (Exception e) {
            sendError(session, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        try {
            MessageDto messageDto = objectMapper.readValue(textMessage.getPayload(), MessageDto.class);
            messageDto = messageService.saveMessage(messageDto);

            List<String> users = chatService.getChatUsers(new ChatIdDto(messageDto.getChat(), messageDto.getLocale())).stream()
                    .map(UserDto::getUsername)
                    .toList();

            for (String username : users) sendToUser(username, messageDto);

            log.info("successful send message");

        } catch (JsonProcessingException | IncorrectUserDataException e) {
            sendError(session, e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (IOException e) {
            sendError(session, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendToUser(String username, MessageDto message) throws IOException {
        WebSocketSession session = sessions.get(username);
        if (session != null && session.isOpen()) {
            log.info("sendMessage: " + message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
        } else if (session == null) {
            log.warn("session == null");
        } else {
            log.info("session closed");
        }
    }

    private void sendError(WebSocketSession session, String error, HttpStatus status) {
        try {
            log.warn(error);
            ResponseMessage responseMessage = ResponseMessage.builder()
                    .time(LocalDateTime.now())
                    .message(error)
                    .status(status.toString())
                    .build();
            String errorJson = objectMapper.writeValueAsString(responseMessage);
            session.sendMessage(new TextMessage(errorJson));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getUsernameFromSession(WebSocketSession session) {
        UriComponents uri = UriComponentsBuilder.fromUri(Objects.requireNonNull(session.getUri())).build();
        String username = uri.getQueryParams().getFirst("username");
        if (username == null) throw new IncorrectUserDataException("Username cannot be empty");
        return username;
    }
}
