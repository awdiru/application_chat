package ru.avdonin.client.client.gui.additional.panels;

import lombok.Getter;
import ru.avdonin.client.client.gui.ConstatntsGUI.ConstantsGUI;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.additional.frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageJPanel extends JPanel {
    private static final Color SELF_MESSAGE_COLOR = new Color(205, 214, 244);
    private static final Color FRIEND_MESSAGE_COLOR = new Color(157, 180, 239);
    private final MainFrame mainFrame;

    @Getter
    private final MessageDto messageDto;
    @Getter
    private JPanel headerPanel;
    @Getter
    private JTextPane textContent;
    @Getter
    private JPanel message;

    public MessageJPanel(MainFrame mainFrame, MessageDto messageDto) {
        super();
        this.messageDto = messageDto;
        this.mainFrame = mainFrame;
        initPanel();
    }

    private void initPanel() {
        Color bgColor = messageDto.getSender().equals(mainFrame.getUsername()) ? SELF_MESSAGE_COLOR : FRIEND_MESSAGE_COLOR;

        headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);

        ImageIcon avatarIcon = mainFrame.getAvatars().computeIfAbsent(messageDto.getSender(), k -> {
            mainFrame.getAvatars().put(messageDto.getSender(), mainFrame.getDictionary().getDefaultAvatar());
            mainFrame.loadAvatarAsync(messageDto.getSender(), MessageJPanel.this);
            return mainFrame.getAvatars().get(k);
        });

        if (avatarIcon != null) {
            JLabel avatarLabel = new JLabel(avatarIcon);
            avatarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            headerPanel.add(avatarLabel, BorderLayout.WEST);
        }

        JTextPane title = FrameHelper.getTextPane();
        String formattedTitle = "<html><div style='padding:2px'>"
                + FrameHelper.formatDateTime(messageDto.getTime())
                + " <b>" + messageDto.getSender() + "</b></div></html>";
        title.setText(formattedTitle);
        headerPanel.add(title, BorderLayout.CENTER);

        JButton actions = new JButton(mainFrame.getDictionary().getBurger());
        actions.addActionListener(e -> showMessageActionsContextMenu(actions, this, messageDto));
        headerPanel.add(actions, BorderLayout.EAST);

        textContent = FrameHelper.getTextPane();
        textContent.setText(messageDto.getTextContent());
        textContent.setSize(new Dimension(250, Short.MAX_VALUE));
        int height = textContent.getPreferredSize().height;

        message = new JPanel();
        message.setLayout(new BorderLayout());
        message.add(headerPanel, BorderLayout.NORTH);
        message.add(textContent, BorderLayout.CENTER);
        message.setBorder(new EmptyBorder(5, 5, 5, 5));

        if (messageDto.getImagesBase64() != null && !messageDto.getImagesBase64().isEmpty()) {
            JPanel images = new JPanel();
            images.setLayout(new BoxLayout(images, BoxLayout.Y_AXIS));
            images.setBackground(bgColor);
            images.setBorder(new EmptyBorder(0, 0, 0, 0));

            for (String receivedImage : messageDto.getImagesBase64()) {
                ImageIcon image = FrameHelper.getIcon(receivedImage, mainFrame.getDictionary());
                JLabel imageLabel = new JLabel(image);

                JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
                container.setBackground(bgColor);
                container.setBorder(new EmptyBorder(0, 0, 5, 0));
                container.add(imageLabel);

                height += container.getPreferredSize().height;
                images.add(container);
            }
            message.add(images, BorderLayout.SOUTH);
        }
        message.setPreferredSize(new Dimension(250, height + 50));

        message.setBackground(bgColor);
        title.setBackground(bgColor);
        textContent.setBackground(bgColor);

        setBackground((Color) ConstantsGUI.BACKGROUND_COLOR.getValue());
        setOpaque(false);

        if (messageDto.getSender().equals(mainFrame.getUsername()))
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        else setLayout(new FlowLayout(FlowLayout.LEFT));

        add(message);
    }

    private void showMessageActionsContextMenu(JButton parent, JPanel message, MessageDto messageDto) {
        JPopupMenu menu = new JPopupMenu();

        if (messageDto.getSender().equals(mainFrame.getUsername())) {
            JMenuItem changeMessage = new JMenuItem();
            changeMessage.setIcon(mainFrame.getDictionary().getPencil());
            changeMessage.setText(mainFrame.getDictionary().getChangeMessage());
            changeMessage.addActionListener(e -> mainFrame.getMessageArea().changeMessage(messageDto));
            menu.add(changeMessage);
        }

        if (messageDto.getSender().equals(mainFrame.getUsername())) {
            JMenuItem deleteMessage = new JMenuItem();
            deleteMessage.setIcon(mainFrame.getDictionary().getDelete());
            deleteMessage.setText(mainFrame.getDictionary().getDeleteMessage());
            deleteMessage.addActionListener(e -> deleteMessageAction(messageDto, message));
            menu.add(deleteMessage);
        }

        menu.show(parent, 0, parent.getHeight());
    }

    private void deleteMessageAction(MessageDto messageDto, JPanel message) {
        try {
            mainFrame.getClient().deleteMessage(messageDto);
            mainFrame.getChatArea().remove(message);
            FrameHelper.repaintComponents(mainFrame.getChatArea());
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame.getDictionary(), mainFrame);
        }
    }
}
