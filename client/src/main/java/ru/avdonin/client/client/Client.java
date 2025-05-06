package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.Setter;
import ru.avdonin.client.settings.language.BaseLanguage;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.template.exceptions.ClientException;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.util.ErrorResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ClientEndpoint
public class Client {
    @Setter
    private MessageListener messageListener;
    private Session session;
    @Setter
    @Getter
    private BaseLanguage language = FactoryLanguage.getFactory().getSettings();

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
                .build();
        System.out.println(username + " " + password + " " + path);
        String requestBody = objectMapper.writeValueAsString(userDto);
        String url = BaseURL + "/user" + path;
        post(url, requestBody);
    }

    public void sendMessage(String content, String username, String recipient) throws IOException {
        MessageDto message = MessageDto.builder()
                .time(null)
                .sender(username)
                .recipient(recipient)
                .content(content)
                .build();

        String json = objectMapper.writeValueAsString(message);
        session.getBasicRemote().sendText(json);
    }

    public List<MessageDto> getChat(String username, String recipient) throws Exception {
        String url = BaseURL + "/chat/get?sender=" + username + "&recipient=" + recipient + "&from=0&size=50";
        HttpResponse<String> response = get(url);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public List<FriendDto> getFriends(String username) throws Exception {
        String url = BaseURL + "/user/friends/get?username=" + username;
        HttpResponse<String> response = get(url);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public void addFriend(String username, String friendName) throws Exception {
        String url = BaseURL + "/user/friends/add?username=" + username + "&friendName=" + friendName;
        post(url, null);
    }

    public List<FriendDto> getRequestsFriends(String username) throws Exception {
        String url = BaseURL + "/user/friends/requests?username=" + username;
        HttpResponse<String> response = get(url);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public void confirmFriend(String username, String friendName, Boolean confirm) throws Exception {
        String url = BaseURL + "/user/friends/confirmed?username=" + username + "&friendName=" + friendName + "&confirm=" + confirm;
        post(url, null);
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }


    private HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private HttpResponse<String> post(String url, String body) throws Exception {
        if (body == null) body = "";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private void errorHandler(HttpResponse<String> response) throws Exception {
        ErrorResponse errorResponse = objectMapper.readValue(response.body(), ErrorResponse.class);
        throw new ClientException(createErrorMessage(errorResponse));
    }

    private String createErrorMessage(ErrorResponse errorResponse) {
        String time = errorResponse.getTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
        return time + " " + language.getErrorCode() + "\n"
                + language.getStatusCode() + ": " + errorResponse.getStatus().toString() + "\n"
                + language.getError() + ": " + errorResponse.getMessage();
    }
}