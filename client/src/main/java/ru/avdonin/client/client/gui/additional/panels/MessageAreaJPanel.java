package ru.avdonin.client.client.gui.additional.panels;

import lombok.Getter;
import ru.avdonin.client.client.gui.ConstatntsGUI.ConstantsGUI;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class MessageAreaJPanel extends JPanel {
    private final Set<String> sentImagesBase64 = new HashSet<>();
    private final Set<String> usersTyping = new HashSet<>();

    private final MainFrame mainFrame;

    private JTextArea textArea;
    private JTextArea userTyping;
    private JButton attachButton;
    private JPanel attachPanel;
    private Timer typingTimer;
    private JButton deleteButton;
    private JButton sendButton;

    private boolean isTyping;
    private boolean isEditMode;

    //Для редактирования сообщений
    private MessageJPanel messageJPanel;

    public MessageAreaJPanel(MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;

        initTypingTimer();
        initMessageAreaJPanel();
    }

    public void addUserTyping(String username, String chatId) {
        if (!chatId.equals(mainFrame.getChat().getId())) return;

        usersTyping.add(username);
        userTyping.setText(getTypingText(usersTyping, mainFrame));
    }

    public void delUserTyping(String username, String chatId) {
        if (!chatId.equals(mainFrame.getChat().getId())) return;

        usersTyping.remove(username);
        userTyping.setText(getTypingText(usersTyping, mainFrame));
    }

    public void stopTyping() {
        try {
            typingTimer.stop();
            if (isTyping) {
                isTyping = false;
                mainFrame.getClient().sendTyping(mainFrame.getChat().getId(), false);
            }
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame.getDictionary(), mainFrame);
        }
    }

    public void clearSent() {
        sentImagesBase64.clear();
        attachPanel.removeAll();
        textArea.setText("");
        FrameHelper.repaintComponents(attachPanel, textArea);
    }

    public void clear() {
        usersTyping.clear();
        userTyping.removeAll();
        FrameHelper.repaintComponents(userTyping);
        clearSent();
    }

    public void changeMessageMode(MessageJPanel messageJPanel) {
        this.messageJPanel = messageJPanel;
        this.isEditMode = true;
        this.textArea.setText(messageJPanel.getMessageDto().getTextContent());
        this.sentImagesBase64.clear();
        this.attachPanel.removeAll();

        if (messageJPanel.getMessageDto().getImagesBase64() != null)
            for (String imageBase64 : messageJPanel.getMessageDto().getImagesBase64()) addImage(imageBase64);
    }

    private void initMessageAreaJPanel() {
        textArea = new JTextArea(4, 27);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        isEditMode = false;
        isTyping = false;
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUserTyping();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUserTyping();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        attachButton = new JButton(mainFrame.getDictionary().getPaperClip());
        attachButton.addActionListener(e -> attachButtonAction());
        attachButton.setPreferredSize(new Dimension(30, 30));

        sendButton = new JButton(mainFrame.getDictionary().getRightArrow());
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setPreferredSize(new Dimension(30, 30));
        setupStandardKeiBindings();

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inputPanel.add(scrollPane);
        inputPanel.add(attachButton);
        inputPanel.add(sendButton);


        attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(attachPanel, BorderLayout.NORTH);
        messagePanel.add(inputPanel, BorderLayout.CENTER);

        userTyping = FrameHelper.getTextArea("", null);

        setLayout(new BorderLayout());
        add(userTyping, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
    }

    private void initTypingTimer() {
        int TYPING_DELAY_MS = 10000;
        typingTimer = new Timer(TYPING_DELAY_MS, e -> {
            try {
                mainFrame.getClient().sendTyping(mainFrame.getChat().getId(), false);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, mainFrame.getDictionary(), mainFrame);
            }
        });
        typingTimer.setRepeats(false);
    }

    private void handleUserTyping() {
        boolean hasText = !textArea.getText().trim().isEmpty();
        try {
            if (hasText) {
                if (!isTyping) {
                    isTyping = true;
                    mainFrame.getClient().sendTyping(mainFrame.getChat().getId(), true);
                }
                typingTimer.restart();
            } else {
                typingTimer.stop();
                if (isTyping) {
                    isTyping = false;
                    mainFrame.getClient().sendTyping(mainFrame.getChat().getId(), false);
                }
            }
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame.getDictionary(), mainFrame);
        }
    }

    private void attachButtonAction() {
        try {
            String image = FrameHelper.attachImage(mainFrame.getDictionary());
            addImage(image);
        } catch (IOException ex) {
            FrameHelper.errorHandler(ex, mainFrame.getDictionary(), mainFrame);
        }
    }

    private void addImage(String imageBase64) {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(new JLabel(FrameHelper.getScaledIcon(imageBase64, 30, 30, mainFrame.getDictionary())), BorderLayout.WEST);
        imagePanel.add(getDeleteButton(imageBase64, imagePanel), BorderLayout.EAST);

        attachPanel.add(imagePanel);
        sentImagesBase64.add(imageBase64);

        FrameHelper.repaintComponents(attachPanel);
    }

    private JButton getDeleteButton(String image, JComponent component) {
        deleteButton = new JButton();
        deleteButton.setIcon(mainFrame.getDictionary().getDelete());
        deleteButton.addActionListener(e -> {
            sentImagesBase64.remove(image);
            attachPanel.remove(component);
            FrameHelper.repaintComponents(attachPanel);
        });
        return deleteButton;
    }

    private String getTypingText(Set<String> usersTyping, MainFrame mainFrame) {
        StringBuilder builder = new StringBuilder();

        if (usersTyping.isEmpty()) return "";

        for (String name : usersTyping)
            builder.append(name).append(", ");

        builder.delete(builder.length() - 2, builder.length());
        builder.append(mainFrame.getDictionary().getTyping());

        return builder.toString();
    }

    private void setupStandardKeiBindings() {
        FrameHelper.setupKeyBindings(textArea,
                (KeyStroke) ConstantsGUI.SEND_MESSAGE.getValue(),
                (String) ConstantsGUI.SEND_MESSAGE_KEY.getValue(),
                e -> sendMessage());
    }

    public void sendMessageAction() {
        SwingUtilities.invokeLater(() -> {
            try {
                stopTyping();
                mainFrame.connect();
                MessageDto messageDto = MessageDto.builder()
                        .time(OffsetDateTime.now())
                        .sender(mainFrame.getUsername())
                        .chatId(mainFrame.getChat().getId())
                        .textContent(textArea.getText())
                        .imagesBase64(sentImagesBase64.isEmpty() ? null : sentImagesBase64)
                        .edited(false)
                        .build();
                if (FrameHelper.isEmptyMessage(messageDto)) return;

                mainFrame.getClient().sendMessage(messageDto);
                mainFrame.onMessageReceived(messageDto);

            } catch (Exception e) {
                FrameHelper.errorHandler(e, mainFrame.getDictionary(), mainFrame);
            }
            clearSent();
        });
    }

    private void changeMessageAction() {
        try {
            isEditMode = false;
            stopTyping();
            mainFrame.connect();

            MessageDto oldMessage = messageJPanel.getMessageDto();

            MessageDto messageDto = MessageDto.builder()
                    .id(oldMessage.getId())
                    .time(oldMessage.getTime())
                    .sender(oldMessage.getSender())
                    .chatId(oldMessage.getChatId())
                    .textContent(Objects.equals(textArea.getText(), oldMessage.getTextContent()) ? null : textArea.getText())
                    .imagesBase64(Objects.equals(sentImagesBase64, oldMessage.getImagesBase64()) ? null : sentImagesBase64)
                    .edited(true)
                    .build();

            if (FrameHelper.isEmptyMessage(messageDto)) return;

            mainFrame.getClient().changeMessage(messageDto);

            messageDto.setTextContent(textArea.getText());
            messageDto.setImagesBase64(sentImagesBase64);

            messageJPanel.init(messageDto);
            FrameHelper.repaintComponents(messageJPanel);

        } catch (Exception ex) {
            FrameHelper.errorHandler(ex, mainFrame.getDictionary(), mainFrame);
        }
        clearSent();
    }

    private void sendMessage() {
        if (isEditMode) {
            changeMessageAction();
        } else {
            sendMessageAction();
        }
    }
}
