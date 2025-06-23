package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.Setter;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.repository.ConfigsRepository;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.client.settings.time_zone.FactoryTimeZone;
import ru.avdonin.template.exceptions.ClientException;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.message.dto.NewMessageDto;
import ru.avdonin.template.model.user.dto.*;
import ru.avdonin.template.model.util.LocaleDto;
import ru.avdonin.template.model.util.ResponseMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ClientEndpoint
public class Client {
    @Setter
    private GUI gui;
    private Session session;
    @Setter
    @Getter
    private BaseDictionary language;
    private String httpURI;
    private String wsURI;

    private final ConfigsRepository configsRepository = new ConfigsRepository();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Client(MainFrame gui) {
        this.gui = gui;
        loadProperties();
        language = FactoryLanguage.getFactory().getSettings();
    }

    public Client() {
        loadProperties();
        language = FactoryLanguage.getFactory().getSettings();
    }

    public void connect(String username) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(wsURI + "/chat?username=" + username));
    }

    public void disconnect() throws IOException {
        session.close();
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) throws Exception {
        NewMessageDto newMessageDto = objectMapper.readValue(message, NewMessageDto.class);
        HttpResponse<String> response = get("/message/get", newMessageDto);
        MessageDto messageDto = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        messageDto.setTime(messageDto.getTime()
                .withOffsetSameInstant(ZoneOffset.ofHours(
                        FactoryTimeZone.getFactory().getFrameSettings().getTimeZoneOffset()
                )));
        gui.onMessageReceived(messageDto);
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
        post("/user" + path, userDto);
    }

    public void sendMessage(MessageDto message) throws Exception {
        message.setLocale(getLocale());
        post("/message/send", message);
    }

    public String getAvatar(String username) throws Exception {
        UserAvatarDto userAvatarDto = UserAvatarDto.builder()
                .username(username)
                .locale(getLocale())
                .build();
        HttpResponse<String> response = get("/user/get/avatar", userAvatarDto);
        UserAvatarDto avatarDto = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        return avatarDto.getAvatarBase64();
    }

    public List<MessageDto> getChatHistory(String chatId) throws Exception {
        return getChatHistory(chatId, 0);
    }

    public List<MessageDto> getChatHistory(String chatId, int from) throws Exception {
        ChatGetHistoryDto chatGetHistoryDto = ChatGetHistoryDto.builder()
                .chatId(chatId)
                .from(from)
                .size(10)
                .locale(getLocale())
                .build();
        HttpResponse<String> response = get("/chat/get/history", chatGetHistoryDto);
        List<MessageDto> messages = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        return messages.stream()
                .peek(message -> {
                    message.setTime(message.getTime()
                            .withOffsetSameInstant(ZoneOffset.ofHours(
                                    FactoryTimeZone.getFactory().getFrameSettings().getTimeZoneOffset()
                            )));
                })
                .toList();
    }

    public List<ChatDto> getChats(String username) throws Exception {
        LocaleDto localeDto = new LocaleDto(getLocale());
        HttpResponse<String> response = get("/chat/get/all?username=" + username, localeDto);
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
        post("/chat/create", chatCreateDto);
    }

    public void logoutOfChat(String username, String chatId) throws Exception {
        ChatParticipantDto chatParticipantDto = ChatParticipantDto.builder()
                .chatId(chatId)
                .username(username)
                .locale(getLocale())
                .build();
        put("/chat/logout", chatParticipantDto);
    }

    public void renameChat(String username, String chatId, String newChatName, boolean isAdmin) throws Exception {
        ChatRenameDto userDto = ChatRenameDto.builder()
                .username(username)
                .chatId(chatId)
                .newChatName(newChatName)
                .locale(getLocale())
                .build();
        put("/chat/rename" + (isAdmin ? "" : "/custom"), userDto);
    }

    public void addUserFromChat(String username, String chatId) throws Exception {
        InvitationChatDto invitationChatDto = InvitationChatDto.builder()
                .chatId(chatId)
                .username(username)
                .locale(getLocale())
                .build();
        post("/chat/add", invitationChatDto);
    }

    public List<InvitationChatDto> getInvitationsChats(String username) throws Exception {
        UsernameDto usernameDto = UsernameDto.builder()
                .username(username)
                .locale(getLocale())
                .build();
        HttpResponse<String> response = get("/chat/get/invitations", usernameDto);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public void confirmInvite(String chatId, String username, boolean isConfirmed) throws Exception {
        InvitationChatDto invitationChatDto = InvitationChatDto.builder()
                .chatId(chatId)
                .username(username)
                .confirmed(isConfirmed)
                .build();

        post("/chat/confirm/invitation", invitationChatDto);
    }

    public List<UserDto> getChatParticipants(String chatId) throws Exception {
        ChatIdDto chatIdDto = ChatIdDto.builder()
                .chatId(chatId)
                .locale(getLocale())
                .build();
        HttpResponse<String> response = get("/chat/get/users", chatIdDto);
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
        HttpResponse<String> response = get("/chat/get/private", userFriendDto);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public ChatDto getPersonalChat(String username) throws Exception {
        UserDto userDto = UserDto.builder()
                .username(username)
                .locale(getLocale())
                .build();
        HttpResponse<String> response = get("/chat/get/personal", userDto);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public void avatarChange(String username, String avatarBase64) throws Exception {
        UserDto userDto = UserDto.builder()
                .username(username)
                .avatarBase64(avatarBase64)
                .locale(getLocale())
                .build();
        post("/user/avatar/change", userDto);
    }

    public UserDto getUserDto(String username) throws Exception {
        UserDto userDto = UserDto.builder()
                .username(username)
                .locale(getLocale())
                .build();

        HttpResponse<String> response = get("/user/get", userDto);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    private HttpResponse<String> get(String method, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .method("GET", getBody(body))
                .uri(getURI(method))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private HttpResponse<String> post(String method, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(getBody(body))
                .uri(getURI(method))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private HttpResponse<String> put(String method, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .method("PUT", getBody(body))
                .uri(getURI(method))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
        return response;
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

    private URI getURI(String method) {
        return URI.create(httpURI + method);
    }

    private HttpRequest.BodyPublisher getBody(Object obj) throws JsonProcessingException {
        String json = obj == null ? "" : objectMapper.writeValueAsString(obj);
        return HttpRequest.BodyPublishers.ofString(json);
    }

    private void loadProperties() {
        this.httpURI = configsRepository.getConfig("http-uri");
        this.wsURI = configsRepository.getConfig("ws-uri");
    }
}