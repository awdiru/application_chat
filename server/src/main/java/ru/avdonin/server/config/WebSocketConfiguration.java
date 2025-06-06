package ru.avdonin.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.avdonin.server.controller.list.MessageHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final MessageHandler messageHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/chat");
    }
}
