package ru.avdonin.client.client.gui.additional_frames.list;

import ru.avdonin.client.client.gui.additional_frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class AddUserFromChatFrame extends BaseAdditionalFrame {
    private final MainFrame parent;

    public AddUserFromChatFrame(MainFrame parent, ChatDto chat) {
        this.parent = parent;

        initFrame(parent.getDictionary().getLogoutChat(),
                new Dimension(250, 150));

        String question = parent.getDictionary().getAddUser() + " " + FrameHelper.getChatName(chat);
        JLabel addLabel = new JLabel("<html><div style='text-align: center;'>" + question + "</div></html>");
        addLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = getAddUserFromChatButtonPanel(chat.getId(), AddUserFromChatFrame.this);

        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.add(addLabel, BorderLayout.NORTH);
        addPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(addPanel);
    }

    private JPanel getAddUserFromChatButtonPanel(String chatId, JFrame main) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton addButton = new JButton();

        JTextField addUserField = new JTextField();

        addButton.addActionListener(e -> {
            try {
                parent.getClient().addUserFromChat(addUserField.getText(), chatId);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, parent.getDictionary(), AddUserFromChatFrame.this);
            } finally {
                main.dispose();
            }
        });
        addButton.setText(parent.getDictionary().getAdd());
        buttonPanel.add(addButton);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        wrapperPanel.add(buttonPanel);

        JPanel endPanel = new JPanel(new BorderLayout());
        endPanel.add(addUserField, BorderLayout.NORTH);
        endPanel.add(wrapperPanel, BorderLayout.SOUTH);
        return endPanel;
    }
}
