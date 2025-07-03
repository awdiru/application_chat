package ru.avdonin.client.client.gui.additional.frames;

import ru.avdonin.client.client.gui.additional.frames.list.*;
import ru.avdonin.template.model.chat.dto.ChatDto;

public abstract class AdditionalFrameFactory {
    public static void getRenameChatFrame(ChatDto renameChat, Boolean isAdmin) {
        new RenameChatFrame(renameChat, isAdmin);
    }

    public static void getLogoutChatFrame(ChatDto deleteChat) {
        new LogoutChatFrame(deleteChat);
    }

    public static void getCreateChatFrame(boolean isPrivate){
        new CreateChatFrame(isPrivate);
    }

    public static void getAddUserFromChatFrame(ChatDto chat){
        new AddUserFromChatFrame(chat);
    }
}
