package ru.avdonin.client.client.gui.additional.frames.list;

import ru.avdonin.client.client.gui.additional.frames.BaseAdditionalFrame;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class RenameChatFrame extends BaseAdditionalFrame {
    private final JTextField renameField;
    private final ChatDto renameChat;
    private final boolean isAdmin;
    
    public RenameChatFrame(ChatDto renameChat, Boolean isAdmin) {
        BaseDictionary dictionary = getDictionary();
        this.renameChat = renameChat;
        this.isAdmin = isAdmin;
        this.renameField = new JTextField();
        
        initFrame(dictionary.getRenameChatCustom() + " " + FrameHelper.getChatName(renameChat),
                new Dimension(250, 150));
        
        JButton renameButton = getRenameButton(dictionary);

        JPanel renamePanel = new JPanel(new BorderLayout());
        renamePanel.add(renameField, BorderLayout.NORTH);
        renamePanel.add(renameButton, BorderLayout.SOUTH);
        add(renamePanel);
    }
    
    private JButton getRenameButton(BaseDictionary dictionary) {
        JButton renameButton = new JButton();
        renameButton.setText(dictionary.getRename());
        renameButton.addActionListener(e -> {
            try {
                if (!renameField.getText().equals(renameChat.getChatName())) {
                    parent.getClient().renameChat(parent.getUsername(), renameChat.getId(), renameField.getText(), isAdmin);
                    parent.loadChats();
                }
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, parent);
            } finally {
                dispose();
            }
        });
        return renameButton;
    }
}
