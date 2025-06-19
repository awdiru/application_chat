package ru.avdonin.client.client.additional_frames;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;

import javax.swing.*;
import java.awt.*;

public class CreateChatFrame extends MainFrame {
    public CreateChatFrame(MainFrame parent, boolean isPrivate) {
        super(parent);
        setTitle(dictionary.getAddChatTitle());
        setSize(240, 110);
        setLocationRelativeTo(null);

        JPanel addChatPanel = new JPanel(new BorderLayout());
        JTextField labelField = new JTextField();
        addChatPanel.add(new JLabel(isPrivate ? dictionary.getFriendsName() : dictionary.getChatName()), BorderLayout.NORTH);
        addChatPanel.add(labelField, BorderLayout.CENTER);

        JButton pubChatButton = new JButton(dictionary.getCreateChat());
        pubChatButton.addActionListener(e -> {
            try {
                client.createChat(username, labelField.getText(), isPrivate);
                parent.loadChats();
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, CreateChatFrame.this);
            }
            dispose();
        });

        addChatPanel.add(pubChatButton, BorderLayout.SOUTH);
        add(addChatPanel);
        setVisible(true);
    }
}
