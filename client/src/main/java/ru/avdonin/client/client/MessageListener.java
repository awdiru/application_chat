package ru.avdonin.client.client;


import ru.avdonin.template.model.message.dto.MessageDto;

public interface MessageListener {
    void start();
    void onMessageReceived(MessageDto message);
}
