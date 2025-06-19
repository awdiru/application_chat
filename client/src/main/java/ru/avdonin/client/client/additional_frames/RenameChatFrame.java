package ru.avdonin.client.client.additional_frames;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class RenameChatFrame extends MainFrame {
    public RenameChatFrame(MainFrame parent, ChatDto renameChat, boolean isAdmin) {
        super(parent);
        setTitle(dictionary.getRenameChatCustom() + " " + MainFrameHelper.getChatName(renameChat));
        setSize(250, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField renameField = new JTextField();

        JButton renameButton = new JButton();
        renameButton.setText(dictionary.getRename());
        renameButton.addActionListener(e -> {
            try {
                if (!renameField.getText().equals(renameChat.getChatName())) {
                    client.renameChat(username, renameChat.getId(), renameField.getText(), isAdmin);
                    parent.loadChats();
                }
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, parent);
            } finally {
                dispose();
            }
        });

        JPanel renamePanel = new JPanel(new BorderLayout());
        renamePanel.add(renameField, BorderLayout.NORTH);
        renamePanel.add(renameButton, BorderLayout.SOUTH);
        add(renamePanel);
        setVisible(true);
    }
}
