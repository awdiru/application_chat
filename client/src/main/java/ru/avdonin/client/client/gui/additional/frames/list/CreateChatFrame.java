package ru.avdonin.client.client.gui.additional.frames.list;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.gui.additional.frames.BaseAdditionalFrame;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client.context.ContextKeys.*;

public class CreateChatFrame extends BaseAdditionalFrame {
    public CreateChatFrame(boolean isPrivate) {
        BaseDictionary dictionary = getDictionary();

        initFrame(dictionary.getAddChatTitle(),
                new Dimension(240, 110));

        JPanel addChatPanel = new JPanel(new BorderLayout());
        JTextField labelField = new JTextField();

        String panelName = isPrivate ? dictionary.getFriendsName() : dictionary.getChatName();
        addChatPanel.add(new JLabel(panelName), BorderLayout.NORTH);
        addChatPanel.add(labelField, BorderLayout.CENTER);

        JButton pubChatButton = getPubChatButton(isPrivate, labelField, dictionary);

        addChatPanel.add(pubChatButton, BorderLayout.SOUTH);
        add(addChatPanel);
    }

    private JButton getPubChatButton(boolean isPrivate, JTextField labelField, BaseDictionary dictionary) {
        JButton pubChatButton = new JButton(dictionary.getCreateChat());
        pubChatButton.addActionListener(e -> {
            try {
                Client client = Context.get(CLIENT);
                String username = Context.get(USERNAME);
                client.createChat(username, labelField.getText(), isPrivate);
                parent.loadChats();

            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, CreateChatFrame.this);
            }
            dispose();
        });
        return pubChatButton;
    }
}
