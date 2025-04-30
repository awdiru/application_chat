package ru.avdonin.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.Setter;
import ru.avdonin.client.model.message.MessageDto;
import ru.avdonin.client.model.user.UserAuthenticationDto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@ClientEndpoint
public class Client {
    @Setter
    protected MessageListener messageListener;
    protected Session session;
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected final ObjectMapper objectMapper = new ObjectMapper()
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

    public List<MessageDto> getChat(String username, String recipient) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/chat/get?sender="
                        + username + "&recipient=" + recipient + "&from=0&size=50"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) throw new IOException("Не удалось получить список сообщений");

        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public boolean login(String username, String password, String path) throws IOException, InterruptedException {
        UserAuthenticationDto userDto = UserAuthenticationDto.builder()
                .username(username)
                .password(password)
                .build();
        String requestBody = objectMapper.writeValueAsString(userDto);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost:8080/user" + path))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.statusCode() == 200;
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}