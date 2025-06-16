package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.Setter;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.client.settings.time_zone.FactoryTimeZone;
import ru.avdonin.template.exceptions.ClientException;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.user.dto.UserDto;
import ru.avdonin.template.model.user.dto.UserFriendDto;
import ru.avdonin.template.model.user.dto.UsernameDto;
import ru.avdonin.template.model.util.LocaleDto;
import ru.avdonin.template.model.util.ResponseMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ClientEndpoint
public class Client {
    @Setter
    private MessageListener messageListener;
    private Session session;
    @Setter
    @Getter
    private BaseDictionary language = FactoryLanguage.getFactory().getSettings();

    private final String BaseURL = "http://localhost:8080";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Client(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public Client() {
    }

    public void connect(String username) throws URISyntaxException, IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/chat?username=" + username;
        container.connectToServer(this, new URI(uri));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
        messageDto.setTime(messageDto.getTime()
                .withOffsetSameInstant(ZoneOffset.ofHours(
                        FactoryTimeZone.getFactory().getFrameSettings().getTimeZoneOffset()
                )));
        messageListener.onMessageReceived(messageDto);
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
    }

    @OnError
    public void onError(Throwable throwable) throws IOException {
        throw new IOException(throwable.getMessage());
    }

    public void login(String username, String password, String path) throws Exception {
        UserAuthenticationDto userDto = UserAuthenticationDto.builder()
                .username(username)
                .password(password)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(userDto);
        String url = BaseURL + "/user" + path;
        post(url, json);
    }

    public void sendMessage(String content, String username, String chatId) throws IOException {
        MessageDto message = MessageDto.builder()
                .sender(username)
                .chat(chatId)
                .content(content)
                .locale(getLocale())
                .build();

        String json = objectMapper.writeValueAsString(message);
        session.getBasicRemote().sendText(json);
    }

    public List<MessageDto> getChatHistory(String chatId) throws Exception {
        ChatGetHistoryDto chatGetHistoryDto = ChatGetHistoryDto.builder()
                .chatId(chatId)
                .from(0)
                .size(10)
                .locale(getLocale())
                .build();

        String url = BaseURL + "/chat/get/history";
        String json = objectMapper.writeValueAsString(chatGetHistoryDto);
        HttpResponse<String> response = get(url, json);
        List<MessageDto> messages = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        return messages.stream()
                .peek(message -> message.setTime(message.getTime()
                        .withOffsetSameInstant(ZoneOffset.ofHours(
                                FactoryTimeZone.getFactory().getFrameSettings().getTimeZoneOffset()
                        ))))
                .toList();
    }

    public List<ChatDto> getChats(String username) throws Exception {
        LocaleDto localeDto = new LocaleDto(getLocale());
        String json = objectMapper.writeValueAsString(localeDto);
        String url = BaseURL + "/chat/get/all?username=" + username;
        HttpResponse<String> response = get(url, json);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public void createChat(String username, String chatName, Boolean privateChat) throws Exception {
        ChatCreateDto chatCreateDto = ChatCreateDto.builder()
                .chatName(chatName)
                .username(username)
                .privateChat(privateChat)
                .locale(getLocale())
                .build();

        String url = BaseURL + "/chat/create";
        String json = objectMapper.writeValueAsString(chatCreateDto);
        post(url, json);
    }

    public void logoutOfChat(String username, String chatId) throws Exception {
        ChatParticipantDto chatParticipantDto = ChatParticipantDto.builder()
                .chatId(chatId)
                .username(username)
                .locale(getLocale())
                .build();
        String url = BaseURL + "/chat/logout";
        String json = objectMapper.writeValueAsString(chatParticipantDto);
        put(url, json);
    }

    public void renameChatCustom(String username, String chatId, String newChatName) throws Exception {
        ChatRenameDto userDto = ChatRenameDto.builder()
                .username(username)
                .chatId(chatId)
                .newChatName(newChatName)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(userDto);
        String url = BaseURL + "/chat/rename/custom";
        put(url, json);
    }

    public void renameChatAdmin(String username, String chatId, String newChatName) throws Exception {
        ChatRenameDto userDto = ChatRenameDto.builder()
                .username(username)
                .chatId(chatId)
                .newChatName(newChatName)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(userDto);
        String url = BaseURL + "/chat/rename";
        put(url, json);
    }

    public void addUserFromChat(String username, String chatId) throws Exception {
        ChatParticipantDto chatParticipantDto = ChatParticipantDto.builder()
                .chatId(chatId)
                .username(username)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(chatParticipantDto);
        String url = BaseURL + "/chat/add";
        post(url, json);
    }
    
    public List<InvitationChatDto> getInvitationsChats(String username) throws Exception {
        UsernameDto usernameDto = UsernameDto.builder()
                .username(username)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(usernameDto);
        String url = BaseURL + "/chat/get/invitations";
        HttpResponse<String> response = get(url, json);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public List<UserDto> getChatParticipants(String chatId) throws Exception {
        ChatIdDto chatIdDto = ChatIdDto.builder()
                .chatId(chatId)
                .locale(getLocale())
                .build();

        String json = objectMapper.writeValueAsString(chatIdDto);
        String url = BaseURL + "/chat/get/users";

        HttpResponse<String> response = get(url, json);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public boolean isNotConnected() {
        return session == null || !session.isOpen();
    }

    public ChatDto getPrivateChat(String username, String friendName) throws Exception {
        UserFriendDto userFriendDto = UserFriendDto.builder()
                .friendName(friendName)
                .username(username)
                .locale(getLocale())
                .build();
        String json = objectMapper.writeValueAsString(userFriendDto);
        String url = BaseURL + "/chat/get/private";
        HttpResponse<String> response = get(url, json);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public ChatDto getPersonalChat(String username) throws Exception {
        UserDto userDto = UserDto.builder()
                .username(username)
                .build();
        String json = objectMapper.writeValueAsString(userDto);
        String url = BaseURL + "/chat/get/personal";
        HttpResponse<String> response = get(url, json);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    private HttpResponse<String> get(String url, String body) throws Exception {
        if (body == null) body = "";
        HttpRequest request = HttpRequest.newBuilder()
                .method("GET", HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private void post(String url, String body) throws Exception {
        if (body == null) body = "";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) errorHandler(response);
    }

    private void put(String url, String body) throws Exception {
        if (body == null) body = "";
        HttpRequest request = HttpRequest.newBuilder()
                .method("PUT", HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
    }

    private void errorHandler(HttpResponse<String> response) throws Exception {
        ResponseMessage responseMessage = objectMapper.readValue(response.body(), ResponseMessage.class);
        throw new ClientException(createErrorMessage(responseMessage));
    }

    private String createErrorMessage(ResponseMessage responseMessage) {
        String time = responseMessage.getTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
        return time + " " + language.getErrorCode() + "\n"
                + language.getStatusCode() + ": " + responseMessage.getStatus() + "\n"
                + language.getError() + ": " + responseMessage.getMessage();
    }
    
    private String getLocale() {
        return FactoryLanguage.getFactory().getSettings().getLocale();
    }
}