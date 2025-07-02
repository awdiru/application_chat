package ru.avdonin.client.client.gui.additional.panels;

import lombok.Getter;
import lombok.Setter;
import ru.avdonin.client.client.gui.ConstatntsGUI.ConstantsGUI;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageJPanel extends JPanel {
    private static final Color SELF_MESSAGE_COLOR = new Color(205, 214, 244);
    private static final Color FRIEND_MESSAGE_COLOR = new Color(157, 180, 239);
    private final MainFrame mainFrame;
    private final Color bgColor;

    @Getter
    @Setter
    private MessageDto messageDto;
    @Getter
    private JPanel headerPanel;
    @Getter
    private JTextArea textPane;
    @Getter
    private JPanel messagePanel;
    @Getter
    private JPanel imagesPanel;

    public MessageJPanel(MainFrame mainFrame, MessageDto messageDto) {
        super();
        this.messageDto = messageDto;
        this.mainFrame = mainFrame;
        this.bgColor = messageDto.getSender().equals(mainFrame.getUsername()) ? SELF_MESSAGE_COLOR : FRIEND_MESSAGE_COLOR;
        init();
    }

    public void init(MessageDto messageDto) {
        this.messageDto = messageDto;
        init();
    }

    public void init() {
        removeAll();
        initHeader();
        initText();
        initImages();
        initMessage();

        setBackground((Color) ConstantsGUI.BACKGROUND_COLOR.getValue());
        setOpaque(false);

        if (messageDto.getSender().equals(mainFrame.getUsername()))
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        else setLayout(new FlowLayout(FlowLayout.LEFT));

        if (messageDto.getEdited()) {
            JTextPane edited = FrameHelper.getTextPane(mainFrame.getDictionary().getEdited());
            edited.setForeground(new Color(165, 170, 170));
            add(edited);
        }
        add(messagePanel);
    }

    private void initHeader() {
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

        String formattedTitle = "<html><div style='padding:2px'>"
                + FrameHelper.formatDateTime(messageDto.getTime())
                + " <b>" + messageDto.getSender() + "</b></div></html>";

        JTextPane title = FrameHelper.getTextPaneHtml(formattedTitle);
        headerPanel.add(title, BorderLayout.CENTER);

        JButton actions = new JButton(mainFrame.getDictionary().getBurger());
        actions.addActionListener(e -> showMessageActionsContextMenu(actions));
        headerPanel.add(actions, BorderLayout.EAST);
        headerPanel.setBackground(bgColor);
    }

    private void showMessageActionsContextMenu(JButton parent) {
        JPopupMenu menu = new JPopupMenu();

        if (messageDto.getSender().equals(mainFrame.getUsername())) {
            JMenuItem changeMessage = new JMenuItem();
            changeMessage.setIcon(mainFrame.getDictionary().getPencil());
            changeMessage.setText(mainFrame.getDictionary().getChangeMessage());
            changeMessage.addActionListener(e -> mainFrame.getMessageArea().changeMessageMode(this));
            menu.add(changeMessage);
        }

        if (messageDto.getSender().equals(mainFrame.getUsername())) {
            JMenuItem deleteMessage = new JMenuItem();
            deleteMessage.setIcon(mainFrame.getDictionary().getDelete());
            deleteMessage.setText(mainFrame.getDictionary().getDeleteMessage());
            deleteMessage.addActionListener(e -> deleteMessageAction(messageDto));
            menu.add(deleteMessage);
        }

        menu.show(parent, 0, parent.getHeight());
    }

    private void deleteMessageAction(MessageDto messageDto) {
        try {
            mainFrame.getClient().deleteMessage(messageDto);
            mainFrame.getChatArea().remove(this);
            FrameHelper.repaintComponents(mainFrame.getChatArea());
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame.getDictionary(), mainFrame);
        }
    }

    private void initText() {
        textPane = null;
        if (messageDto.getTextContent() != null && !messageDto.getTextContent().isEmpty()) {
            textPane = FrameHelper.getTextArea(messageDto.getTextContent(), null);
            textPane.setSize(new Dimension(250, Short.MAX_VALUE));
            textPane.setBackground(bgColor);
        }
    }

    private void initImages() {
        imagesPanel = null;
        if (messageDto.getImagesBase64() != null && !messageDto.getImagesBase64().isEmpty()) {
            imagesPanel = new JPanel();
            imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));
            imagesPanel.setBackground(bgColor);
            imagesPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

            for (String receivedImage : messageDto.getImagesBase64()) {
                ImageIcon image = FrameHelper.getIcon(receivedImage, mainFrame.getDictionary());
                JLabel imageLabel = new JLabel(image);

                JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
                container.setBackground(bgColor);
                container.setBorder(new EmptyBorder(5, 0, 0, 0));
                container.add(imageLabel);

                imagesPanel.add(container);
            }
        }
    }

    private void initMessage() {
        messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());

        messagePanel.add(headerPanel, BorderLayout.NORTH);
        if (textPane != null) messagePanel.add(textPane, BorderLayout.CENTER);
        if (imagesPanel != null) messagePanel.add(imagesPanel, BorderLayout.SOUTH);

        messagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        messagePanel.setBackground(bgColor);
    }
}
