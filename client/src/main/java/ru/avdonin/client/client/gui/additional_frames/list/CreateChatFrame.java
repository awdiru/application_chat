package ru.avdonin.client.client.gui.additional_frames.list;

import ru.avdonin.client.client.gui.additional_frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;

import javax.swing.*;
import java.awt.*;

public class CreateChatFrame extends BaseAdditionalFrame {
    public CreateChatFrame(MainFrame parent, boolean isPrivate) {
        initFrame(parent.getDictionary().getAddChatTitle(),
                new Dimension(240, 110));

        JPanel addChatPanel = new JPanel(new BorderLayout());
        JTextField labelField = new JTextField();
        addChatPanel.add(new JLabel(isPrivate ? parent.getDictionary().getFriendsName() : parent.getDictionary().getChatName()), BorderLayout.NORTH);
        addChatPanel.add(labelField, BorderLayout.CENTER);

        JButton pubChatButton = new JButton(parent.getDictionary().getCreateChat());
        pubChatButton.addActionListener(e -> {
            try {
                parent.getClient().createChat(parent.getUsername(), labelField.getText(), isPrivate);
                parent.loadChats();
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, parent.getDictionary(), CreateChatFrame.this);
            }
            dispose();
        });

        addChatPanel.add(pubChatButton, BorderLayout.SOUTH);
        add(addChatPanel);
    }
}
