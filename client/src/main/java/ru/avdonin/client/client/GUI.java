package ru.avdonin.client.client;

import ru.avdonin.template.model.message.dto.MessageDto;

public interface GUI {
    void onMessageReceived(MessageDto message);
    void loadChats();
}
