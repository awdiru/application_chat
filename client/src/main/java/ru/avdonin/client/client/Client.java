package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Getter;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.time_zone.BaseTimeZone;
import ru.avdonin.client.repository.ConfigsRepository;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.client.client.settings.dictionary.FactoryDictionary;
import ru.avdonin.template.exceptions.ClientException;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.util.ActionNotification;
import ru.avdonin.template.model.user.dto.*;
import ru.avdonin.template.model.util.LocaleDto;
import ru.avdonin.template.model.util.ResponseMessage;
import ru.avdonin.template.model.util.TypingDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.avdonin.client.client.context.ContextKeysEnum.*;
import static ru.avdonin.client.client.context.ContextKeysEnum.TIME_ZONE;
import static ru.avdonin.client.repository.configs.DefaultConfigs.*;

@ClientEndpoint
public class Client {
    private final ConfigsRepository configsRepository = Context.get(CONFIG_REP);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Session session;
    @Getter
    private String httpURI;
    private String wsURI;


    public Client() {
        loadProperties();
    }

    public void connect() throws IOException, DeploymentException {
        String username = Context.get(USERNAME);
        if (isNotConnected()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(wsURI + "/chat?username=" + username));
            if (isNotConnected())
                throw new NoConnectionServerException("There is no connection to the server");
        }
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
        ActionNotification<?> actionNotification = objectMapper.readValue(message, ActionNotification.class);

        MainFrame mainFrame = Context.get(MAIN_FRAME);
        switch (actionNotification.getAction()) {
            case MESSAGE -> messageAction(actionNotification, mainFrame);
            case INVITATION -> mainFrame.loadInvitations();
            case TYPING -> typingAction(actionNotification, mainFrame);
        }
    }

    private void typingAction(ActionNotification<?> actionNotification, MainFrame mainFrame) {
        ActionNotification.Typing typing;

        if (actionNotification.getData() instanceof ActionNotification.Typing)
            typing = (ActionNotification.Typing) actionNotification.getData();
        else throw new RuntimeException("The typing notification contains incorrect information");

        if (typing.getIsTyping())
            mainFrame.getMessageArea().addUserTyping(typing.getUsername(), typing.getChatId());
        else mainFrame.getMessageArea().delUserTyping(typing.getUsername(), typing.getChatId());
    }

    private void messageAction(ActionNotification<?> actionNotification, MainFrame mainFrame) throws Exception {
        ActionNotification.Message message;

        if (actionNotification.getData() instanceof ActionNotification.Message)
            message = (ActionNotification.Message) actionNotification.getData();
        else throw new RuntimeException("The message notification contains incorrect information");

        if (!message.getChatId().equals(mainFrame.getSelectedChat().getChat().getId())) {
            mainFrame.getChatItemJPanels().get(message.getChatId()).addNotificationChat();
            return;
        }

        MessageDto requestDto = MessageDto.builder()
                .id(message.getMessageId())
                .build();

        HttpResponse<String> response = get("/message/get", requestDto);

        MessageDto messageDto = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        BaseTimeZone timeZone = getTimeZone();
        messageDto.setTime(messageDto.getTime()
                .withOffsetSameInstant(ZoneOffset.ofHours(
                        timeZone.getOffset()
                )));
        mainFrame.onMessageReceived(messageDto);
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
        BaseTimeZone timeZone = getTimeZone();
        return messages.stream()
                .peek(message -> {
                    message.setTime(message.getTime()
                            .withOffsetSameInstant(ZoneOffset.ofHours(
                                    timeZone.getOffset()
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

    public List<InvitationChatDto> getInvitationsChats() throws Exception {
        UsernameDto usernameDto = UsernameDto.builder()
            .username(Context.get(USERNAME))
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

    public void deleteMessage(MessageDto messageDto) throws Exception {
        post("/message/delete", messageDto);
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

    public void changeMessage(MessageDto messageDto) throws Exception {
        post("/message/change", messageDto);
    }

    public void sendTyping(String chatId, boolean isTyping) throws Exception {
        TypingDto typingDto = TypingDto.builder()
                .chatId(chatId)
                .username(Context.get(USERNAME))
                .isTyping(isTyping)
                .locale(getLocale())
                .build();

        post("/typing", typingDto);
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
        BaseDictionary dictionary = getDictionary();
        String time = responseMessage.getTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
        return time + " " + dictionary.getErrorCode() + "\n"
                + dictionary.getStatusCode() + ": " + responseMessage.getStatus() + "\n"
                + dictionary.getError() + ": " + responseMessage.getMessage();
    }

    private String getLocale() {
        return FactoryDictionary.getFactory().getSettings().getLocale();
    }

    private URI getURI(String method) {
        return URI.create(httpURI + method);
    }

    private HttpRequest.BodyPublisher getBody(Object obj) throws JsonProcessingException {
        String json = obj == null ? "" : objectMapper.writeValueAsString(obj);
        return HttpRequest.BodyPublishers.ofString(json);
    }

    private void loadProperties() {
        this.httpURI = configsRepository.getConfig(HTTP_URI.getConfigName());
        this.wsURI = configsRepository.getConfig(WS_URI.getConfigName());
    }

    private BaseDictionary getDictionary() {
        return Context.get(DICTIONARY);
    }

    private BaseTimeZone getTimeZone() {
        return Context.get(TIME_ZONE);
    }
}