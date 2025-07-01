package ru.avdonin.client.client.gui.additional.frames;

import ru.avdonin.client.client.gui.additional.frames.list.*;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.message.dto.MessageDto;

public abstract class AdditionalFrameFactory {
    public static void getRenameChatFrame(MainFrame parent, ChatDto renameChat, Boolean isAdmin) {
        new RenameChatFrame(parent, renameChat, isAdmin);
    }

    public static void getLogoutChatFrame(MainFrame parent, ChatDto deleteChat) {
        new LogoutChatFrame(parent, deleteChat);
    }

    public static void getCreateChatFrame(MainFrame parent, boolean isPrivate){
        new CreateChatFrame(parent, isPrivate);
    }

    public static void getAddUserFromChatFrame(MainFrame parent, ChatDto chat){
        new AddUserFromChatFrame(parent, chat);
    }

    public static void getChangeMessageFrame(MainFrame parent, MessageDto messageDto) {
        new ChangeMessageFrame(parent, messageDto);
    }
}
