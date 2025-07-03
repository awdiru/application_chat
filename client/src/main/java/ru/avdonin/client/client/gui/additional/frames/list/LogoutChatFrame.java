package ru.avdonin.client.client.gui.additional.frames.list;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.gui.additional.frames.BaseAdditionalFrame;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client.context.ContextKeys.*;

public class LogoutChatFrame extends BaseAdditionalFrame {
    public LogoutChatFrame(ChatDto deleteChat) {
        BaseDictionary dictionary = getDictionary();

        initFrame(dictionary.getLogoutChat(),
                new Dimension(250, 150));

        String question = dictionary.getLogoutChatQuestion() + " " + FrameHelper.getChatName(deleteChat);
        JLabel deleteLabel = new JLabel("<html><div style='text-align: center;'>" + question + "</div></html>");
        deleteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = getLogoutChatButtonPanel(deleteChat.getId(), dictionary);

        JPanel deletePanel = new JPanel(new BorderLayout());
        deletePanel.add(deleteLabel, BorderLayout.NORTH);
        deletePanel.add(buttonPanel, BorderLayout.SOUTH);
        add(deletePanel);
    }

    private JPanel getLogoutChatButtonPanel(String deleteChatId, BaseDictionary dictionary) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        JButton yesButton = getYesButton(deleteChatId, dictionary);

        JButton noButton = new JButton();
        noButton.addActionListener(e -> dispose());
        noButton.setText(dictionary.getNo());

        buttonPanel.add(yesButton, BorderLayout.WEST);
        buttonPanel.add(noButton, BorderLayout.EAST);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        wrapperPanel.add(buttonPanel);
        return wrapperPanel;
    }

    private JButton getYesButton(String deleteChatId, BaseDictionary dictionary) {
        JButton yesButton = new JButton();
        yesButton.addActionListener(e -> {
            try {
                Client client = Context.get(CLIENT);
                String username = Context.get(USERNAME);

                client.logoutOfChat(username, deleteChatId);
                parent.loadChats();

            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, parent);
            } finally {
                dispose();
            }
        });
        yesButton.setText(dictionary.getYes());
        return yesButton;
    }
}
