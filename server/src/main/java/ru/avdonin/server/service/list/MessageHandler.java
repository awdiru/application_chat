package ru.avdonin.server.service.list;

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
import ru.avdonin.server.entity_model.ChatParticipant;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.ChatParticipantRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.exceptions.IncorrectDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.util.ActionNotification;
import ru.avdonin.template.model.util.ResponseMessage;
import ru.avdonin.template.model.util.actions.list.MessageAct;
import ru.avdonin.template.model.util.actions.list.TypingAct;

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
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String username = getUsernameFromSession(session);
            userRepository.findByUsername(username)
                    .orElseThrow(() -> new IncorrectDataException("User " + username + " not found"));
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

    }

    public void sendToUsersMessage(ActionNotification<?> actionNotification) throws IOException {
        MessageAct messageAct;

        if (actionNotification.getData() instanceof MessageAct)
            messageAct = (MessageAct) actionNotification.getData();
        else throw new RuntimeException("The notification contains incorrect information");

        List<String> users = getUsers(messageAct.getChatId());
        sendUsers(users, actionNotification, messageAct.getSender());
    }

    public void sendToUsersTyping(ActionNotification<?> actionNotification) throws IOException {
        TypingAct typingAct;

        if (actionNotification.getData() instanceof TypingAct)
            typingAct = (TypingAct) actionNotification.getData();
        else throw new RuntimeException("The notification contains incorrect information");

        List<String> users = getUsers(typingAct.getChatId());
        sendUsers(users, actionNotification, typingAct.getUsername());
    }

    public void sendToUser(String username, ActionNotification<?> message) throws IOException {
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private String getUsernameFromSession(WebSocketSession session) {
        UriComponents uri = UriComponentsBuilder.fromUri(Objects.requireNonNull(session.getUri())).build();
        String username = uri.getQueryParams().getFirst("username");
        if (username == null) throw new IncorrectUserDataException("Username cannot be empty");
        return username;
    }

    private void sendUsers(List<String> users, ActionNotification actionNotification, String sender) throws IOException {
        for (String user : users)
            if (!user.equals(sender))
                sendToUser(user, actionNotification);
    }

    private List<String> getUsers(String chatId) {
        return chatParticipantRepository.findAllParticipant(chatId).stream()
                .map(ChatParticipant::getUser)
                .map(User::getUsername)
                .toList();
    }
}
