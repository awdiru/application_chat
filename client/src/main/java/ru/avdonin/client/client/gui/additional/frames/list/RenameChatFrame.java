package ru.avdonin.client.client.gui.additional.frames.list;

import ru.avdonin.client.client.gui.additional.frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class RenameChatFrame extends BaseAdditionalFrame {
    private final MainFrame parent;
    private final JTextField renameField;
    private final ChatDto renameChat;
    private final boolean isAdmin;
    
    public RenameChatFrame(MainFrame parent, ChatDto renameChat, Boolean isAdmin) {
        this.parent = parent;
        this.renameChat = renameChat;
        this.isAdmin = isAdmin;
        this.renameField = new JTextField();
        
        initFrame(parent.getDictionary().getRenameChatCustom() + " " + FrameHelper.getChatName(renameChat),
                new Dimension(250, 150));
        
        JButton renameButton = getRenameButton();

        JPanel renamePanel = new JPanel(new BorderLayout());
        renamePanel.add(renameField, BorderLayout.NORTH);
        renamePanel.add(renameButton, BorderLayout.SOUTH);
        add(renamePanel);
    }
    
    private JButton getRenameButton() {
        JButton renameButton = new JButton();
        renameButton.setText(parent.getDictionary().getRename());
        renameButton.addActionListener(e -> {
            try {
                if (!renameField.getText().equals(renameChat.getChatName())) {
                    parent.getClient().renameChat(parent.getUsername(), renameChat.getId(), renameField.getText(), isAdmin);
                    parent.loadChats();
                }
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, parent.getDictionary(), parent);
            } finally {
                dispose();
            }
        });
        return renameButton;
    }
}
