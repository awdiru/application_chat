package ru.avdonin.client.client.additional_frames;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class LogoutChatFrame extends MainFrame {
    public LogoutChatFrame(MainFrame parent, ChatDto deleteChat) {
        super(parent);
        setTitle(dictionary.getLogoutChat());
        setSize(250, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
       // add(getMainWindow());

        String question = dictionary.getLogoutChatQuestion() + " " + MainFrameHelper.getChatName(deleteChat);
        JLabel deleteLabel = new JLabel("<html><div style='text-align: center;'>" + question + "</div></html>");
        deleteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = getLogoutChatButtonPanel(deleteChat.getId(), LogoutChatFrame.this);

        JPanel deletePanel = new JPanel(new BorderLayout());
        deletePanel.add(deleteLabel, BorderLayout.NORTH);
        deletePanel.add(buttonPanel, BorderLayout.SOUTH);
        add(deletePanel);
    }

    private JPanel getLogoutChatButtonPanel(String deleteChatId, JFrame main) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        JButton yesButton = new JButton();
        yesButton.addActionListener(e -> {
            try {
                client.logoutOfChat(username, deleteChatId);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, LogoutChatFrame.this);
            } finally {
                main.dispose();
            }
        });
        yesButton.setText(dictionary.getYes());

        JButton noButton = new JButton();
        noButton.addActionListener(e -> main.dispose());
        noButton.setText(dictionary.getNo());

        buttonPanel.add(yesButton, BorderLayout.WEST);
        buttonPanel.add(noButton, BorderLayout.EAST);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        wrapperPanel.add(buttonPanel);
        return wrapperPanel;
    }
}
