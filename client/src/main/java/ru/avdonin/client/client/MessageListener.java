package ru.avdonin.client.client;

import ru.avdonin.client.model.message.MessageDto;

public interface MessageListener {
    void start();
    void onMessageReceived(MessageDto message);
}
