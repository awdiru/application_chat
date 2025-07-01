package ru.avdonin.client.client.gui.additional.frames.list;

import ru.avdonin.client.client.gui.additional.frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class LogoutChatFrame extends BaseAdditionalFrame {
    private final MainFrame parent;

    public LogoutChatFrame(MainFrame parent, ChatDto deleteChat) {
        this.parent = parent;

        initFrame(parent.getDictionary().getLogoutChat(),
                new Dimension(250, 150));

        String question = parent.getDictionary().getLogoutChatQuestion() + " " + FrameHelper.getChatName(deleteChat);
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
                parent.getClient().logoutOfChat(parent.getUsername(), deleteChatId);
                parent.loadChats();
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, parent.getDictionary(), parent);
            } finally {
                main.dispose();
            }
        });
        yesButton.setText(parent.getDictionary().getYes());

        JButton noButton = new JButton();
        noButton.addActionListener(e -> main.dispose());
        noButton.setText(parent.getDictionary().getNo());

        buttonPanel.add(yesButton, BorderLayout.WEST);
        buttonPanel.add(noButton, BorderLayout.EAST);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        wrapperPanel.add(buttonPanel);
        return wrapperPanel;
    }
}
