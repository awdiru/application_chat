package ru.avdonin.client.client.gui.additional.panels.list.items;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.additional.frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.additional.panels.BaseJPanel;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ru.avdonin.client.client.constatnts.Constants.*;

public class ChatItemPanel extends BaseJPanel {
    private final JLabel newMessageLabel = new JLabel();
    @Getter
    private final ChatDto chat;
    private Integer newMessageCount = 0;

    public ChatItemPanel(ChatDto chat) {
        this.chat = chat;
        try {
            BaseDictionary dictionary = getDictionary();
            Client client = getClient();

            Integer unreadMessagesCount = Math.toIntExact(client.getUnreadMessagesCount(chat.getId()));
            setNewMessageCount(unreadMessagesCount);
            createChatItem(dictionary);

        } catch (Exception e) {
            FrameHelper.errorHandler(e, getMainFrame());
        }
    }


    public void addNotificationChat() {
        newMessageLabel.setIcon(FrameHelper.getNumber(++newMessageCount));
        FrameHelper.repaintComponents(this);
    }

    public void delNotificationChat() {
        newMessageCount = 0;
        newMessageLabel.setIcon(FrameHelper.getNumber(0));
        FrameHelper.repaintComponents(this);
    }

    private void setNewMessageCount(Integer newMessageCount) {
        this.newMessageCount = newMessageCount - 1;
        addNotificationChat();
    }

    private void createChatItem(BaseDictionary dictionary) {
        MouseAdapter selectListener = getSelectListener();
        JTextArea chatName = FrameHelper.getTextArea(FrameHelper.getChatName(chat), selectListener);

        JButton menuButton = new JButton(dictionary.getBurger());
        menuButton.addActionListener(e -> showChatContextMenu(menuButton, dictionary));

        JPanel containerCN = new JPanel(new BorderLayout());
        containerCN.add(newMessageLabel);

        add(containerCN, BorderLayout.WEST);
        add(chatName, BorderLayout.CENTER);
        add(menuButton, BorderLayout.EAST);

        setMaximumSize(new Dimension(10000, 40));
        addMouseListener(selectListener);
        setOpaque(true);

    }

    private MouseAdapter getSelectListener() {
        MainFrame mainFrame = getMainFrame();
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    try {
                        mainFrame.findChat(chat, ChatItemPanel.this);
                    } catch (Exception ex) {
                        FrameHelper.errorHandler(ex, mainFrame);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setBackground(MOUSE_ENTERED_ITEM_COLOR.getValue());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setBackground(BACKGROUND_COLOR.getValue());
            }
        };
    }

    private void showChatContextMenu(JComponent parent, BaseDictionary dictionary) {
        String username = getUsername();
        JPopupMenu menu = new JPopupMenu();

        if (!chat.getPrivateChat()) {
            JMenuItem addUserItem = new JMenuItem();
            addUserItem.setText(dictionary.getAddUser());
            addUserItem.setIcon(dictionary.getParticipants());
            addUserItem.addActionListener(e -> AdditionalFrameFactory.getAddUserFromChatFrame(chat));
            menu.add(addUserItem);
        }
        if (chat.getAdmin().equals(username) && !chat.getPrivateChat()) {
            JMenuItem renameItemAdmin = new JMenuItem();
            renameItemAdmin.setText(dictionary.getRenameChatAdmin());
            renameItemAdmin.setIcon(dictionary.getPencil());
            renameItemAdmin.addActionListener(e ->
                    AdditionalFrameFactory.getRenameChatFrame(chat, true));
            menu.add(renameItemAdmin);
        }

        JMenuItem renameItemCustom = new JMenuItem();
        if (chat.getPrivateChat())
            renameItemCustom.setText(dictionary.getRename());
        else renameItemCustom.setText(dictionary.getRenameChatCustom());
        renameItemCustom.setIcon(dictionary.getPencil());
        renameItemCustom.addActionListener(e ->
                AdditionalFrameFactory.getRenameChatFrame(chat, false));
        menu.add(renameItemCustom);

        JMenuItem removeItem = new JMenuItem();
        if (chat.getPrivateChat()) {
            removeItem.setText(dictionary.getDeleteChat());
            removeItem.setIcon(dictionary.getDelete());
        } else {
            removeItem.setText(dictionary.getLogoutChat());
            removeItem.setIcon(dictionary.getExit());
        }
        removeItem.addActionListener(e -> logoutChat(chat));
        menu.add(removeItem);

        menu.show(parent, 0, parent.getHeight());
    }

    private void logoutChat(ChatDto deleteChat) {
        MainFrame mainFrame = getMainFrame();
        AdditionalFrameFactory.getLogoutChatFrame(deleteChat);
        if (chat != null && deleteChat.getId().equals(chat.getId()))
            mainFrame.getChatArea().removeAll();
        FrameHelper.repaintComponents(mainFrame.getChatsContainer());
    }
}
