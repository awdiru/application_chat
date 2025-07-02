package ru.avdonin.client.client.gui.additional.panels.list;

import lombok.Getter;
import ru.avdonin.client.client.gui.additional.frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.additional.panels.BaseJPanel;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ru.avdonin.client.client.constatnts.Constants.*;

public class ChatItemJPanel extends BaseJPanel {
    @Getter
    private final ChatDto chat;

    private Integer newMessageCount = 0;
    private JLabel newMessageLabel;

    public ChatItemJPanel(ChatDto chat) {
        this.chat = chat;
        createChatItem();
    }

    private void createChatItem() {
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    try {
                        mainFrame.findChat(chat, ChatItemJPanel.this);
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
        JTextArea chatName = FrameHelper.getTextArea(FrameHelper.getChatName(chat), selectListener);

        JButton menuButton = new JButton(dictionary.getBurger());
        menuButton.addActionListener(e -> showChatContextMenu(menuButton));

        newMessageLabel = new JLabel(FrameHelper.getNumber(0));
        JPanel containerCN = new JPanel(new BorderLayout());
        containerCN.add(newMessageLabel);

        add(containerCN, BorderLayout.WEST);
        add(chatName, BorderLayout.CENTER);
        add(menuButton, BorderLayout.EAST);

        setMaximumSize(new Dimension(10000, 40));
        addMouseListener(selectListener);
        setOpaque(true);

    }

    private void showChatContextMenu(JComponent parent) {
        JPopupMenu menu = new JPopupMenu();

        if (!chat.getPrivateChat()) {
            JMenuItem addUserItem = new JMenuItem();
            addUserItem.setText(dictionary.getAddUser());
            addUserItem.setIcon(dictionary.getParticipants());
            addUserItem.addActionListener(e -> AdditionalFrameFactory.getAddUserFromChatFrame(chat));
            menu.add(addUserItem);
        }
        if (chat.getAdmin().equals(mainFrame.getUsername()) && !chat.getPrivateChat()) {
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
        AdditionalFrameFactory.getLogoutChatFrame(deleteChat);
        if (chat != null && deleteChat.getId().equals(chat.getId()))
            mainFrame.getChatArea().removeAll();
        FrameHelper.repaintComponents(mainFrame.getChatsContainer());
    }

    public void addNotificationChat() {
        newMessageLabel.setIcon(FrameHelper.getNumber(++newMessageCount));
        FrameHelper.repaintComponents(newMessageLabel);
    }

    public void delNotificationChat() {
        newMessageCount = 0;
        newMessageLabel.setIcon(FrameHelper.getNumber(0));
        FrameHelper.repaintComponents(newMessageLabel);
    }
}
