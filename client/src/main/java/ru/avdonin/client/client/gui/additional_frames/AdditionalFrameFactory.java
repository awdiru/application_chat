package ru.avdonin.client.client.gui.additional_frames;

import ru.avdonin.client.client.gui.additional_frames.list.AddUserFromChatFrame;
import ru.avdonin.client.client.gui.additional_frames.list.CreateChatFrame;
import ru.avdonin.client.client.gui.additional_frames.list.LogoutChatFrame;
import ru.avdonin.client.client.gui.additional_frames.list.RenameChatFrame;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.template.model.chat.dto.ChatDto;

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
}
