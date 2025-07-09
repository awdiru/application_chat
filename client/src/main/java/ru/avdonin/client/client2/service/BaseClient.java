package ru.avdonin.client.client2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.client.client.settings.time_zone.BaseTimeZone;
import ru.avdonin.client.repository.ConfigsRepository;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.util.ResponseMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;

import static ru.avdonin.client.client.context.ContextKeysEnum.*;
import static ru.avdonin.client.client.context.ContextKeysEnum.TIME_ZONE;
import static ru.avdonin.client.repository.configs.DefaultConfigs.*;

@ClientEndpoint
public class BaseClient {
    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final ConfigsRepository configsRepository = Context.get(CONFIG_REP);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private Session session;
    private String httpURI;
    private String wsURI;

    public BaseClient() {
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

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) throws Exception {
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
    }

    @OnError
    public void onError(Throwable throwable) throws IOException {
        throw new IOException(throwable.getMessage());
    }

    public void disconnect() throws IOException {
        session.close();
    }

    public boolean isNotConnected() {
        return session == null || !session.isOpen();
    }

    protected HttpResponse<String> get(String path, Object body) throws Exception {
        HttpRequest request = createRequest(HttpMethods.GET, path, body);
        return processResponse(request);
    }

    protected HttpResponse<String> post(String path, Object body) throws Exception {
        HttpRequest request = createRequest(HttpMethods.POST, path, body);
        return processResponse(request);
    }

    protected HttpResponse<String> put(String path, Object body) throws Exception {
        HttpRequest request = createRequest(HttpMethods.PUT, path, body);
        return processResponse(request);
    }

    protected HttpResponse<String> delete(String path, Object body) throws Exception {
        HttpRequest request = createRequest(HttpMethods.DELETE, path, body);
        return processResponse(request);
    }

    protected BaseDictionary getDictionary() {
        return Context.get(DICTIONARY);
    }

    protected BaseTimeZone getTimeZone() {
        return Context.get(TIME_ZONE);
    }

    private HttpRequest createRequest(HttpMethods method, String path, Object body) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .method(method.toString(), getBody(body))
                .uri(getURI(path))
                .header("Content-Type", "application/json")
                .build();
    }

    private HttpResponse<String> processResponse(HttpRequest request) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) errorHandler(response);
        return response;
    }

    private URI getURI(String method) {
        return URI.create(httpURI + method);
    }

    private HttpRequest.BodyPublisher getBody(Object obj) throws JsonProcessingException {
        String json = obj == null ? "" : objectMapper.writeValueAsString(obj);
        return HttpRequest.BodyPublishers.ofString(json);
    }

    private void errorHandler(HttpResponse<String> response) throws Exception {
        ResponseMessage responseMessage = objectMapper.readValue(response.body(), ResponseMessage.class);
        throw new RuntimeException(createErrorMessage(responseMessage));
    }

    private String createErrorMessage(ResponseMessage responseMessage) {
        BaseDictionary dictionary = getDictionary();
        String time = responseMessage.getTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
        return time + "\n" + dictionary.getErrorCode() + ": " + responseMessage.getStatus() + "\n"
                + dictionary.getError() + ": " + responseMessage.getMessage();
    }

    private void loadProperties() {
        this.httpURI = configsRepository.getConfig(HTTP_URI.getConfigName());
        this.wsURI = configsRepository.getConfig(WS_URI.getConfigName());
    }

    private enum HttpMethods {
        GET,
        POST,
        PUT,
        DELETE
    }
}
