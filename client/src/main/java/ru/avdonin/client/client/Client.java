package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ClientEndpoint
public class Client {
    @Setter
    private MessageListener messageListener;
    private Session session;
    @Setter
    @Getter
    private BaseDictionary language = FactoryLanguage.getFactory().getSettings();

    private String httpURI;
    private String wsURI;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Client(MessageListener messageListener) {
        this.messageListener = messageListener;
        loadPropertiesFromYaml();
    }

    public Client() {
        loadPropertiesFromYaml();
    }

    public void connect(String username) throws URISyntaxException, IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(wsURI + "/chat?username=" + username));
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
    public void onClose(CloseReason closeReason) throws IOException {
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

    public void sendMessage(String content, String username, String chatId) throws IOException {
        MessageDto message = MessageDto.builder()
                .sender(username)
                .chatId(chatId)
                .content(content)
                .locale(getLocale())
                .build();

        String json = objectMapper.writeValueAsString(message);
        session.getBasicRemote().sendText(json);
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

    public void renameChatCustom(String username, String chatId, String newChatName) throws Exception {
        ChatRenameDto userDto = ChatRenameDto.builder()
                .username(username)
                .chatId(chatId)
                .newChatName(newChatName)
                .locale(getLocale())
                .build();
        put("/chat/rename/custom", userDto);
    }

    public void renameChatAdmin(String username, String chatId, String newChatName) throws Exception {
        ChatRenameDto userDto = ChatRenameDto.builder()
                .username(username)
                .chatId(chatId)
                .newChatName(newChatName)
                .locale(getLocale())
                .build();
        put("/chat/rename", userDto);
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
                .build();
        HttpResponse<String> response = get("/chat/get/personal", userDto);
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

    private void loadPropertiesFromYaml() {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("application.yml");

        if (inputStream == null) {
            throw new RuntimeException("Файл application.yml не найден!");
        }
        Map<String, Object> yamlMap = yaml.load(inputStream);
        Map<String, Object> encryptionConfig = (Map<String, Object>) yamlMap.get("connection");
        if (encryptionConfig != null) {
            String property = (String) encryptionConfig.get("http-uri");
            this.httpURI = property == null ? "http://localhost:8080" : property;
            property = (String) encryptionConfig.get("ws-uri");
            this.wsURI = property == null ? "ws://localhost:8080" : property;
        } else {
            throw new RuntimeException("Раздел 'connection' отсутствует в application.yml");
        }
    }
}