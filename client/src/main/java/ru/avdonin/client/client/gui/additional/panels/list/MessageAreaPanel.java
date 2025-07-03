package ru.avdonin.client.client.gui.additional.panels.list;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.additional.panels.BaseJPanel;
import ru.avdonin.client.client.gui.additional.panels.list.elements.MessageItemPanel;
import ru.avdonin.client.client.helpers.FrameHelper;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
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

import static ru.avdonin.client.client.constatnts.Constants.*;

@Getter
public class MessageAreaPanel extends BaseJPanel {
    private final Set<String> sentImagesBase64 = new HashSet<>();
    private final Set<String> usersTyping = new HashSet<>();


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
    private MessageItemPanel messageItemPanel;

    public MessageAreaPanel() {
        BaseDictionary dictionary = getDictionary();
        MainFrame mainFrame = getMainFrame();
        Client client = getClient();

        initTypingTimer(mainFrame, client);
        initMessageAreaJPanel(dictionary, mainFrame, client);
    }

    public void addUserTyping(String username, String chatId) {
        MainFrame mainFrame = getMainFrame();
        if (!chatId.equals(mainFrame.getSelectedChat().getChat().getId())) return;

        usersTyping.add(username);
        userTyping.setText(getTypingText(usersTyping));
    }

    public void delUserTyping(String username, String chatId) {
        MainFrame mainFrame = getMainFrame();
        if (!chatId.equals(mainFrame.getSelectedChat().getChat().getId())) return;

        usersTyping.remove(username);
        userTyping.setText(getTypingText(usersTyping));
    }

    public void clear() {
        usersTyping.clear();
        userTyping.removeAll();
        FrameHelper.repaintComponents(userTyping);
        clearSent();
    }

    private void clearSent() {
        sentImagesBase64.clear();
        attachPanel.removeAll();
        textArea.setText("");
        FrameHelper.repaintComponents(attachPanel, textArea);
    }

    public void changeMessageMode(MessageItemPanel messageItemPanel) {
        this.messageItemPanel = messageItemPanel;
        this.isEditMode = true;
        this.textArea.setText(messageItemPanel.getMessageDto().getTextContent());
        this.sentImagesBase64.clear();
        this.attachPanel.removeAll();

        if (messageItemPanel.getMessageDto().getImagesBase64() != null)
            for (String imageBase64 : messageItemPanel.getMessageDto().getImagesBase64()) addImage(imageBase64);
    }

    private void stopTyping(MainFrame mainFrame, Client client) {
        try {
            typingTimer.stop();
            if (isTyping) {
                isTyping = false;
                client.sendTyping(mainFrame.getSelectedChat().getChat().getId(), false);
            }
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame);
        }
    }

    private void initMessageAreaJPanel(BaseDictionary dictionary, MainFrame mainFrame, Client client) {
        textArea = new JTextArea(4, 27);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        isEditMode = false;
        isTyping = false;
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUserTyping(mainFrame, client);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUserTyping(mainFrame, client);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        attachButton = new JButton(dictionary.getPaperClip());
        attachButton.addActionListener(e -> attachButtonAction());
        attachButton.setPreferredSize(new Dimension(30, 30));

        sendButton = new JButton(dictionary.getRightArrow());
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

    private void initTypingTimer(MainFrame mainFrame, Client client) {
        int TYPING_DELAY_MS = 10000;
        typingTimer = new Timer(TYPING_DELAY_MS, e -> {
            try {
                client.sendTyping(mainFrame.getSelectedChat().getChat().getId(), false);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, mainFrame);
            }
        });
        typingTimer.setRepeats(false);
    }

    private void handleUserTyping(MainFrame mainFrame, Client client) {
        boolean hasText = !textArea.getText().trim().isEmpty();
        try {
            if (hasText) {
                if (!isTyping) {
                    isTyping = true;
                    client.sendTyping(mainFrame.getSelectedChat().getChat().getId(), true);
                }
                typingTimer.restart();
            } else {
                typingTimer.stop();
                if (isTyping) {
                    isTyping = false;
                    client.sendTyping(mainFrame.getSelectedChat().getChat().getId(), false);
                }
            }
        } catch (Exception e) {
            FrameHelper.errorHandler(e, mainFrame);
        }
    }

    private void attachButtonAction() {
        MainFrame mainFrame = getMainFrame();
        try {
            String image = FrameHelper.attachImage();
            addImage(image);
        } catch (IOException ex) {
            FrameHelper.errorHandler(ex, mainFrame);
        }
    }

    private void addImage(String imageBase64) {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(new JLabel(FrameHelper.getScaledIcon(imageBase64, 30, 30)), BorderLayout.WEST);
        imagePanel.add(getDeleteButton(imageBase64, imagePanel), BorderLayout.EAST);

        attachPanel.add(imagePanel);
        sentImagesBase64.add(imageBase64);

        FrameHelper.repaintComponents(attachPanel);
    }

    private JButton getDeleteButton(String image, JComponent component) {
        BaseDictionary dictionary = getDictionary();
        deleteButton = new JButton();
        deleteButton.setIcon(dictionary.getDelete());
        deleteButton.addActionListener(e -> {
            sentImagesBase64.remove(image);
            attachPanel.remove(component);
            FrameHelper.repaintComponents(attachPanel);
        });
        return deleteButton;
    }

    private String getTypingText(Set<String> usersTyping) {
        BaseDictionary dictionary = getDictionary();
        StringBuilder builder = new StringBuilder();

        if (usersTyping.isEmpty()) return "";

        for (String name : usersTyping)
            builder.append(name).append(", ");

        builder.delete(builder.length() - 2, builder.length());
        builder.append(usersTyping.size() > 1 ? dictionary.getTypings() : dictionary.getTyping());

        return builder.toString();
    }

    private void setupStandardKeiBindings() {
        FrameHelper.setupKeyBindings(textArea,
                SEND_MESSAGE.getValue(),
                SEND_MESSAGE_KEY.getValue(),
                e -> sendMessage());
    }

    public void sendMessageAction() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = getMainFrame();
            String username = getUsername();
            Client client = getClient();

            try {
                stopTyping(mainFrame, client);
                client.connect();
                MessageDto messageDto = MessageDto.builder()
                        .time(OffsetDateTime.now())
                        .sender(username)
                        .chatId(mainFrame.getSelectedChat().getChat().getId())
                        .textContent(textArea.getText())
                        .imagesBase64(sentImagesBase64.isEmpty() ? null : sentImagesBase64)
                        .edited(false)
                        .build();
                if (FrameHelper.isEmptyMessage(messageDto)) return;

                client.sendMessage(messageDto);
                mainFrame.onMessageReceived(messageDto);

            } catch (Exception e) {
                FrameHelper.errorHandler(e, mainFrame);
            }
            clearSent();
        });
    }

    private void changeMessageAction() {
        MainFrame mainFrame = getMainFrame();
        Client client = getClient();
        try {
            isEditMode = false;
            stopTyping(mainFrame, client);
            client.connect();

            MessageDto oldMessage = messageItemPanel.getMessageDto();

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

            client.changeMessage(messageDto);

            messageDto.setTextContent(textArea.getText());
            messageDto.setImagesBase64(sentImagesBase64);

            messageItemPanel.init(messageDto);
            FrameHelper.repaintComponents(messageItemPanel);

        } catch (Exception ex) {
            FrameHelper.errorHandler(ex, mainFrame);
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
