package ru.avdonin.client.client.additional_frames;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.awt.*;

public class AddUserFromChatFrame extends MainFrame {
    public AddUserFromChatFrame(MainFrame parent, ChatDto chat) {
        super(parent);
        setTitle(dictionary.getLogoutChat());
        setSize(250, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
       // add(getMainWindow());

        String question = dictionary.getAddUser() + " " + MainFrameHelper.getChatName(chat);
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
                client.addUserFromChat(addUserField.getText(), chatId);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, AddUserFromChatFrame.this);
            } finally {
                main.dispose();
            }
        });
        addButton.setText(dictionary.getAdd());
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
