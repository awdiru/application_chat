package ru.avdonin.client.client.gui;

import jakarta.websocket.DeploymentException;
import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.additional_frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
import ru.avdonin.client.repository.NotificationRepository;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.template.constatns.Constants;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.chat.dto.InvitationChatDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

@Getter
public class MainFrame extends JFrame {
    private final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    private final Map<String, ImageIcon> avatars = new HashMap<>();
    private final Map<String, JLabel> chatIconsNotification = new HashMap<>();
    private final Map<String, Integer> chatNotifications = new HashMap<>();
    private final Set<String> sentImagesBase64 = new HashSet<>();
    private final List<MessageDto> messages = new ArrayList<>();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final Client client;
    private final String username;

    private Integer chatHistoryCount = 1;

    private JPanel chatArea;
    private JPanel invitationsContainer;
    private JPanel chatsContainer;
    private JPanel attachPanel;
    private JTextArea userTyping;

    private JTextArea chatName;
    private JTextArea messageArea;

    private JScrollPane chatScroll;
    private JButton attachButton;
    private ChatDto chat;

    private boolean isTyping = false;
    private Timer typingTimer;
    private final int TYPING_DELAY_MS = 10000;


    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        this.avatars.put(username, getAvatarIcon());

        client.setGui(this);

        initUi();
        loadChats();
        loadInvitations();
        setVisible(true);
    }

    public void onMessageReceived(MessageDto message) {
        if (!message.getChatId().equals(chat.getId())) return;

        chatArea.add(createMessageItem(message));

        FrameHelper.repaintComponents(chatArea);

        SwingUtilities.invokeLater(() -> {
            JScrollBar scrollBar = chatScroll.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        });
    }

    public void loadChats() {
        new SwingWorker<List<ChatDto>, Void>() {
            @Override
            protected List<ChatDto> doInBackground() {
                try {
                    return client.getChats(username);
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    chatsContainer.removeAll();
                    for (ChatDto c : get()) chatsContainer.add(createChatItem(c));
                    if (chat == null) findChat(get().getFirst());
                    FrameHelper.repaintComponents(chatsContainer);
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void initUi() {
        setTitle(dictionary.getChat() + " - " + username);
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(getMainWindow());
    }

    private void sendMessage() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    typingTimer.stop();
                    if (isTyping) {
                        isTyping = false;
                        client.sendTyping(chat.getId(), false);
                    }

                    connect();
                    MessageDto messageDto = MessageDto.builder()
                            .time(OffsetDateTime.now())
                            .sender(username)
                            .chatId(chat.getId())
                            .textContent(messageArea.getText())
                            .imagesBase64(sentImagesBase64.isEmpty() ? null : sentImagesBase64)
                            .build();
                    if ((messageDto.getTextContent() == null || messageDto.getTextContent().isEmpty())
                            && (messageDto.getImagesBase64() == null || messageDto.getImagesBase64().isEmpty()))
                        return null;

                    client.sendMessage(messageDto);

                    attachPanel.removeAll();
                    FrameHelper.repaintComponents(attachPanel);

                    onMessageReceived(messageDto);
                    sentImagesBase64.clear();
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return null;
            }

            @Override
            protected void done() {
                messageArea.setText("");
            }
        }.execute();
    }

    public void loadChatHistory() {
        new SwingWorker<List<MessageDto>, Void>() {
            @Override
            protected List<MessageDto> doInBackground() {
                try {
                    return client.getChatHistory(chat.getId());
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    chatArea.removeAll();
                    for (MessageDto m : get())
                        chatArea.add(createMessageItem(m));

                    messages.clear();
                    FrameHelper.repaintComponents(chatArea);
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void loadChatHistory(int from) {
        new SwingWorker<List<MessageDto>, Void>() {
            @Override
            protected List<MessageDto> doInBackground() {
                try {
                    return client.getChatHistory(chat.getId(), from);
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    if (get().isEmpty()) return;

                    JScrollBar verticalBar = chatScroll.getVerticalScrollBar();
                    int currentScrollPosition = verticalBar.getValue();

                    messages.addAll(0, get());

                    List<JPanel> newMessages = new ArrayList<>();
                    int totalHeight = 0;

                    for (MessageDto m : get()) {
                        JPanel messageItem = createMessageItem(m);
                        newMessages.add(messageItem);
                        totalHeight += messageItem.getPreferredSize().height;
                    }

                    for (int i = newMessages.size() - 1; i >= 0; i--)
                        chatArea.add(newMessages.get(i), 0);

                    FrameHelper.repaintComponents(chatArea);

                    int newScrollPosition = currentScrollPosition + totalHeight - 30;
                    SwingUtilities.invokeLater(() -> {
                        verticalBar.setValue(newScrollPosition);
                    });

                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    public void loadInvitations() {
        new SwingWorker<List<InvitationChatDto>, Void>() {
            @Override
            protected List<InvitationChatDto> doInBackground() {
                try {
                    return client.getInvitationsChats();
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    invitationsContainer.removeAll();
                    for (InvitationChatDto inv : get()) invitationsContainer.add(createInvitationItem(inv));
                    FrameHelper.repaintComponents(invitationsContainer);
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void showCreateChatContextMenu(JComponent parent) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem privateChat = new JMenuItem();
        privateChat.setText(dictionary.getPrivateChat());
        privateChat.addActionListener(e -> AdditionalFrameFactory.getCreateChatFrame(MainFrame.this, true));
        menu.add(privateChat);

        JMenuItem publicChat = new JMenuItem();
        publicChat.setText(dictionary.getPublicChat());
        publicChat.addActionListener(e -> AdditionalFrameFactory.getCreateChatFrame(MainFrame.this, false));
        menu.add(publicChat);

        menu.show(parent, 0, parent.getHeight());
    }


    private JPanel getChatPanel() {
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(backgroungColor);

        chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(500, 500));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        return chatPanel;
    }


    private JPanel getMessagePanel() {
        messageArea = new JTextArea(4, 27);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        initTypingTimer();

        messageArea.getDocument().addDocumentListener(new DocumentListener() {
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

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        attachButton = new JButton(dictionary.getPaperClip());
        attachButton.addActionListener(e -> attachButtonAction());
        attachButton.setPreferredSize(new Dimension(30, 30));

        JButton sendButton = new JButton(dictionary.getRightArrow());
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setPreferredSize(new Dimension(30, 30));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inputPanel.add(scrollPane);
        inputPanel.add(attachButton);
        inputPanel.add(sendButton);

        setupKeyBindings();

        attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(attachPanel, BorderLayout.NORTH);
        messagePanel.add(inputPanel, BorderLayout.CENTER);

        userTyping = getTextArea("", null);

        JPanel container = new JPanel(new BorderLayout());
        container.add(userTyping, BorderLayout.NORTH);
        container.add(messagePanel, BorderLayout.CENTER);

        return container;
    }

    private void handleUserTyping() {
        boolean hasText = !messageArea.getText().trim().isEmpty();
        try {
            if (hasText) {
                if (!isTyping) {
                    isTyping = true;
                    client.sendTyping(chat.getId(), true);
                }
                typingTimer.restart();
            } else {
                typingTimer.stop();
                if (isTyping) {
                    isTyping = false;
                    client.sendTyping(chat.getId(), false);
                }
            }
        } catch (Exception e) {
            FrameHelper.errorHandler(e, dictionary, MainFrame.this);
        }
    }

    private void initTypingTimer() {
        typingTimer = new Timer(TYPING_DELAY_MS, e -> {
            try {
                isTyping = false;
                client.sendTyping(chat.getId(), false);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        typingTimer.setRepeats(false);
    }

    private void attachButtonAction() {
        try {
            String image = FrameHelper.attachImage(dictionary);

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.add(new JLabel(FrameHelper.getScaledIcon(image, 30, 30, dictionary)), BorderLayout.WEST);
            imagePanel.add(getDeleteButton(image, imagePanel), BorderLayout.EAST);

            attachPanel.add(imagePanel);
            sentImagesBase64.add(image);
            FrameHelper.repaintComponents(attachPanel);

        } catch (IOException ex) {
            FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
        }
    }

    private JButton getDeleteButton(String image, JComponent component) {
        JButton deleteButton = new JButton();
        deleteButton.setIcon(dictionary.getDelete());
        deleteButton.addActionListener(e -> {
            sentImagesBase64.remove(image);
            attachPanel.remove(component);
            FrameHelper.repaintComponents(attachPanel);
        });
        return deleteButton;
    }

    private void setupKeyBindings() {
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER,
                InputEvent.CTRL_DOWN_MASK
        );

        messageArea.getInputMap().put(ctrlEnter, "sendAction");
        messageArea.getActionMap().put("sendAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private JSplitPane getLeftPanel() {
        JPanel chatsPanel = getChatsPanel();
        JPanel invitationsPanel = getInvitationsPanel();

        JSplitPane leftPanel = new JSplitPane();
        leftPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftPanel.setTopComponent(chatsPanel);
        leftPanel.setBottomComponent(invitationsPanel);

        return leftPanel;
    }

    private JPanel getChatsPanel() {
        chatsContainer = new JPanel();
        chatsContainer.setLayout(new BoxLayout(chatsContainer, BoxLayout.Y_AXIS));

        JScrollPane chatsScrollPane = new JScrollPane(chatsContainer);

        JButton addChatButton = new JButton(dictionary.getNewChat());
        addChatButton.addActionListener(e -> showCreateChatContextMenu(addChatButton));

        JPanel headerChatsPanel = new JPanel(new BorderLayout());


        String spase;
        ImageIcon avatar = avatars.get(username);
        if (avatar != null) {
            headerChatsPanel.add(new JLabel(avatar), BorderLayout.WEST);
            spase = " ";
        } else spase = "";

        headerChatsPanel.add(new JLabel(spase + username), BorderLayout.CENTER);
        headerChatsPanel.add(addChatButton, BorderLayout.EAST);

        JPanel chatsPanel = new JPanel(new BorderLayout());
        chatsPanel.add(headerChatsPanel, BorderLayout.NORTH);
        chatsPanel.add(chatsScrollPane, BorderLayout.CENTER);
        chatsPanel.setMinimumSize(new Dimension(1000, 300));

        return chatsPanel;
    }

    private ImageIcon getAvatarIcon() {
        try {
            UserDto userDto = client.getUserDto(username);
            return FrameHelper.getScaledIcon(userDto.getAvatarBase64(),
                    (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                    (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                    dictionary);
        } catch (Exception ignored) {
        }
        return null;
    }

    private JPanel getInvitationsPanel() {
        invitationsContainer = new JPanel();
        invitationsContainer.setLayout(new BoxLayout(invitationsContainer, BoxLayout.Y_AXIS));

        JScrollPane invitationsScrollPane = new JScrollPane(invitationsContainer);

        JButton rebootInvitationsButton = new JButton(dictionary.getReboot());
        rebootInvitationsButton.addActionListener(e -> {
            try {
                loadInvitations();
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });

        JPanel headerInvitationsPanel = new JPanel(new BorderLayout());
        headerInvitationsPanel.add(new JLabel(dictionary.getInvitations()), BorderLayout.CENTER);
        headerInvitationsPanel.add(rebootInvitationsButton, BorderLayout.EAST);

        JPanel invitationsPanel = new JPanel(new BorderLayout());
        invitationsPanel.add(headerInvitationsPanel, BorderLayout.NORTH);
        invitationsPanel.add(invitationsScrollPane, BorderLayout.CENTER);

        return invitationsPanel;
    }

    private final Color mouseEnteredItemColor = new Color(183, 250, 211);
    private final Color backgroungColor = UIManager.getColor("Panel.background");

    private JPanel createChatItem(ChatDto chat) {
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    try {
                        if (MainFrame.this.chat.equals(chat)) return;
                        connect();
                        findChat(chat);
                    } catch (Exception ex) {
                        FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setBackground(mouseEnteredItemColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setBackground(backgroungColor);
            }
        };
        JTextArea textArea = getTextArea(FrameHelper.getChatName(chat), selectListener);

        JButton menuButton = new JButton(dictionary.getBurger());
        menuButton.addActionListener(e -> showChatContextMenu(menuButton, chat));

        initNotificationNum(chat.getId());

        JPanel containerCN = new JPanel(new BorderLayout());
        containerCN.add(chatIconsNotification.get(chat.getId()), BorderLayout.NORTH);

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(containerCN, BorderLayout.WEST);
        itemPanel.add(textArea, BorderLayout.CENTER);
        itemPanel.add(menuButton, BorderLayout.EAST);

        itemPanel.setMaximumSize(new Dimension(10000, 40));
        itemPanel.addMouseListener(selectListener);
        itemPanel.setOpaque(true);

        return itemPanel;
    }

    private JPanel createInvitationItem(InvitationChatDto invitation) {
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setBackground(mouseEnteredItemColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setBackground(backgroungColor);
            }
        };

        JTextArea textArea = getTextArea(invitation.getChatName(), selectListener);

        JButton menuButton = new JButton(dictionary.getBurger());
        menuButton.addActionListener(e -> showInvitationsContextMenu(menuButton, invitation));

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(textArea, BorderLayout.CENTER);
        itemPanel.add(menuButton, BorderLayout.EAST);

        itemPanel.setMaximumSize(new Dimension(10000, 40));
        itemPanel.addMouseListener(selectListener);
        itemPanel.setOpaque(true);

        return itemPanel;
    }


    private JPanel createMessageItem(MessageDto messageDto) {
        Color selfMessage = new Color(205, 214, 244);
        Color friendMessage = new Color(157, 180, 239);

        Color bgColor = messageDto.getSender().equals(username) ? selfMessage : friendMessage;


        if (!avatars.containsKey(messageDto.getSender())) {
            avatars.put(messageDto.getSender(), dictionary.getDefaultAvatar());
            loadAvatarAsync(messageDto.getSender());
        }

        ImageIcon avatarIcon = avatars.get(messageDto.getSender());

        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);

        if (avatarIcon != null) {
            JLabel avatarLabel = new JLabel(avatarIcon);
            avatarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            headerPanel.add(avatarLabel, BorderLayout.WEST);
        }

        JTextPane title = getTextPane();

        String formattedTitle = "<html><div style='padding:2px'>"
                + FrameHelper.formatDateTime(messageDto.getTime())
                + " <b>" + messageDto.getSender() + "</b></div></html>";
        title.setText(formattedTitle);
        headerPanel.add(title, BorderLayout.CENTER);

        JTextPane textContent = getTextPane();
        textContent.setText(messageDto.getTextContent());
        textContent.setSize(new Dimension(250, Short.MAX_VALUE));
        int height = textContent.getPreferredSize().height;

        JPanel message = new JPanel();
        message.setLayout(new BorderLayout());
        message.add(headerPanel, BorderLayout.NORTH);
        message.add(textContent, BorderLayout.CENTER);
        message.setBorder(new EmptyBorder(5, 5, 5, 5));

        if (messageDto.getImagesBase64() != null) {
            JPanel images = new JPanel();
            images.setLayout(new BoxLayout(images, BoxLayout.Y_AXIS));
            images.setBackground(bgColor);
            images.setBorder(new EmptyBorder(0, 0, 0, 0));

            for (String receivedImage : messageDto.getImagesBase64()) {
                ImageIcon image = FrameHelper.getIcon(receivedImage, dictionary);
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

        JPanel container = new JPanel();
        container.setBackground(backgroungColor);
        container.setOpaque(false);

        if (messageDto.getSender().equals(username))
            container.setLayout(new FlowLayout(FlowLayout.RIGHT));
        else container.setLayout(new FlowLayout(FlowLayout.LEFT));

        container.add(message);

        JButton actions = new JButton(dictionary.getBurger());
        actions.addActionListener(e -> showMessageActionsContextMenu(actions, container, messageDto));
        headerPanel.add(actions, BorderLayout.EAST);

        return container;
    }

    private void loadAvatarAsync(String username) {
        new SwingWorker<ImageIcon, Void>() {

            @Override
            protected ImageIcon doInBackground() {
                try {
                    String avatarBase64 = client.getAvatar(username);
                    return FrameHelper.getScaledIcon(avatarBase64,
                            (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                            (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                            dictionary);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon avatar = get();
                    avatars.get(username).setImage(avatar.getImage());
                    FrameHelper.repaintComponents(chatArea);
                } catch (Exception ignored) {
                }
            }
        }.execute();

    }

    private void showMessageActionsContextMenu(JButton parent, JPanel message, MessageDto messageDto) {
        JPopupMenu menu = new JPopupMenu();

        if (messageDto.getSender().equals(username)) {
            JMenuItem changeMessage = new JMenuItem();
            changeMessage.setIcon(dictionary.getPencil());
            changeMessage.setText(dictionary.getChangeMessage());
            changeMessage.addActionListener(e -> AdditionalFrameFactory.getChangeMessageFrame(MainFrame.this, messageDto));
            menu.add(changeMessage);
        }

        if (messageDto.getSender().equals(username)) {
            JMenuItem deleteMessage = new JMenuItem();
            deleteMessage.setIcon(dictionary.getDelete());
            deleteMessage.setText(dictionary.getDeleteMessage());
            deleteMessage.addActionListener(e -> deleteMessageAction(messageDto, message));
            menu.add(deleteMessage);
        }

        menu.show(parent, 0, parent.getHeight());
    }

    private void deleteMessageAction(MessageDto messageDto, JPanel message) {
        try {
            client.deleteMessage(messageDto);
            chatArea.remove(message);
            FrameHelper.repaintComponents(chatArea);
        } catch (Exception e) {
            FrameHelper.errorHandler(e, dictionary, MainFrame.this);
        }
    }

    private JTextPane getTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(null);
        textPane.setMargin(new Insets(0, 0, 0, 0));
        return textPane;
    }

    private JTextArea getTextArea(String title, MouseAdapter selectListener) {
        JTextArea textArea = new JTextArea(title);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        textArea.setBackground(backgroungColor);
        textArea.addMouseListener(selectListener);
        return textArea;
    }

    private void showChatContextMenu(JComponent parent, ChatDto chat) {
        JPopupMenu menu = new JPopupMenu();
        if (!chat.getPrivateChat()) {
            JMenuItem addUserItem = new JMenuItem();
            addUserItem.setText(dictionary.getAddUser());
            addUserItem.setIcon(dictionary.getParticipants());
            addUserItem.addActionListener(e -> AdditionalFrameFactory.getAddUserFromChatFrame(MainFrame.this, chat));
            menu.add(addUserItem);
        }
        if (chat.getAdmin().equals(username) && !chat.getPrivateChat()) {
            JMenuItem renameItemAdmin = new JMenuItem();
            renameItemAdmin.setText(dictionary.getRenameChatAdmin());
            renameItemAdmin.setIcon(dictionary.getPencil());
            renameItemAdmin.addActionListener(e ->
                    AdditionalFrameFactory.getRenameChatFrame(MainFrame.this, chat, true));
            menu.add(renameItemAdmin);
        }

        JMenuItem renameItemCustom = new JMenuItem();
        if (chat.getPrivateChat())
            renameItemCustom.setText(dictionary.getRename());
        else renameItemCustom.setText(dictionary.getRenameChatCustom());
        renameItemCustom.setIcon(dictionary.getPencil());
        renameItemCustom.addActionListener(e ->
                AdditionalFrameFactory.getRenameChatFrame(MainFrame.this, chat, false));
        menu.add(renameItemCustom);

        JMenuItem removeItem = new JMenuItem();
        if (chat.getPrivateChat()) {
            removeItem.setText(dictionary.getDeleteChat());
            removeItem.setIcon(dictionary.getDelete());
        } else {
            removeItem.setText(dictionary.getLogoutChat());
            removeItem.setIcon(dictionary.getExit());
        }
        removeItem.addActionListener(e -> logoutChat(chat));
        menu.add(removeItem);

        menu.show(parent, 0, parent.getHeight());
    }

    private void showInvitationsContextMenu(JComponent parent, InvitationChatDto invitationChatDto) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem confirmInvite = new JMenuItem();
        confirmInvite.setText(dictionary.getConfirmInvite());
        confirmInvite.addActionListener(e -> {
            try {
                client.confirmInvite(invitationChatDto.getChatId(), username, true);
                loadChats();
                loadInvitations();
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        menu.add(confirmInvite);

        JMenuItem rejectInvite = new JMenuItem();
        rejectInvite.setText(dictionary.getRejectInvite());
        rejectInvite.addActionListener(e -> {
            try {
                client.confirmInvite(invitationChatDto.getChatId(), username, false);
                loadChats();
                loadInvitations();
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        menu.add(rejectInvite);

        menu.show(parent, 0, parent.getHeight());
    }

    private void logoutChat(ChatDto deleteChat) {
        AdditionalFrameFactory.getLogoutChatFrame(MainFrame.this, deleteChat);
        if (chat != null && deleteChat.getId().equals(chat.getId()))
            chatArea.removeAll();
        FrameHelper.repaintComponents(chatsContainer);
    }

    private void renameChat(ChatDto chat, boolean isAdmin) {
        AdditionalFrameFactory.getRenameChatFrame(MainFrame.this, chat, isAdmin);
    }

    private JPanel getStatusBar() {
        //Панель кнопок
        JPanel buttonsPanel = new JPanel();
        //Перезагрузить
        JButton restart = new JButton(dictionary.getReboot());
        restart.addActionListener(e -> {
            avatars.clear();
            FrameHelper.restart(MainFrame.this, client, username);
        });
        buttonsPanel.add(restart);
        //Настройки
        JButton settings = new JButton(dictionary.getSettings());
        settings.addActionListener(e -> Settings.getFrameSettings());
        buttonsPanel.add(settings);
        //Сменить аватар
        JButton changeAvatarButton = new JButton(dictionary.getChangeAvatar());
        changeAvatarButton.addActionListener(e -> changeAvatar());
        buttonsPanel.add(changeAvatarButton);
        //Выйти
        JButton exit = new JButton(dictionary.getExit());
        exit.addActionListener(e -> {
            try {
                dispose();
                client.disconnect();
                sentImagesBase64.clear();
                avatars.clear();
                new LoginFrame(client);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(exit);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setSize(new Dimension(800, 100));
        statusBar.add(buttonsPanel, BorderLayout.WEST);
        return statusBar;
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(MainFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String base64Image = Base64.getEncoder().encodeToString(fileContent);

                client.avatarChange(username, base64Image);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        }
    }

    private JPanel getChatStatusBar() {
        JPanel buttonsPanel = new JPanel();

        JButton messagesButton = new JButton(dictionary.getUpArrow());
        messagesButton.addActionListener(e -> {
            try {
                loadChatHistory(chatHistoryCount++);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(messagesButton);

        JButton participantsButton = new JButton(dictionary.getParticipants());
        participantsButton.addActionListener(e -> {
            try {
                showParticipantsContextMenu(participantsButton);
            } catch (Exception ex) {
                FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(participantsButton);

        chatName = getTextArea(FrameHelper.getChatName(chat), null);

        JPanel chatStatusBar = new JPanel(new BorderLayout());
        chatStatusBar.add(chatName, BorderLayout.CENTER);
        chatStatusBar.add(buttonsPanel, BorderLayout.EAST);
        return chatStatusBar;
    }

    private void showParticipantsContextMenu(JComponent parent) throws Exception {
        JPopupMenu participants = new JPopupMenu();
        if (chat == null || chat.getId().isEmpty()) return;

        List<UserDto> users = client.getChatParticipants(chat.getId());
        for (UserDto user : users) {
            JMenuItem userItem = new JMenuItem();

            userItem.setIcon(FrameHelper.getScaledIcon(user.getAvatarBase64(), 16, 16, dictionary));
            userItem.setText(user.getUsername());

            participants.add(userItem);
            userItem.addActionListener(e -> handleUserSelection(user));
        }
        participants.show(parent, 0, parent.getHeight());
    }

    private void handleUserSelection(UserDto user) {
        try {
            ChatDto newChat = user.getUsername().equals(username)
                    ? client.getPersonalChat(username)
                    : client.getPrivateChat(username, user.getUsername());

            if (chat.equals(newChat)) return;

            findChat(chat);
        } catch (Exception ex) {
            FrameHelper.errorHandler(ex, dictionary, MainFrame.this);
        }
    }

    private JPanel getRightPanel() {
        JPanel chatStatusBar = getChatStatusBar();
        JPanel chatPanel = getChatPanel();
        JPanel messagePanel = getMessagePanel();

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatStatusBar, BorderLayout.NORTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messagePanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JSplitPane getChatAppSplitPane() {
        JPanel rightPanel = getRightPanel();
        JSplitPane leftPanel = getLeftPanel();

        JSplitPane mainWindow = new JSplitPane();
        mainWindow.setDividerLocation(200);
        mainWindow.setLeftComponent(leftPanel);
        mainWindow.setRightComponent(rightPanel);

        return mainWindow;
    }

    private JSplitPane getMainWindow() {
        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane mainWindow = getChatAppSplitPane();
        JPanel statusBar = getStatusBar();

        main.setTopComponent(statusBar);
        main.setBottomComponent(mainWindow);
        return main;
    }

    private void connect() throws DeploymentException, IOException {
        if (client.isNotConnected()) {
            client.connect(username);
            if (client.isNotConnected())
                throw new NoConnectionServerException("There is no connection to the server");
        }
    }

    public void addNotificationChat(String chatId) {
        Integer numNotification = -1;
        try {
            numNotification = notificationRepository.getNotificationsNum(chatId);
        } catch (Exception ignored) {
        }
        notificationRepository.updateOrCreateNotificationNum(chatId, ++numNotification);

        ImageIcon num = FrameHelper.getNumber(numNotification, dictionary);
        JLabel chatIcon = chatIconsNotification.computeIfAbsent(chatId, k -> new JLabel(num));
        chatIcon.setIcon(num);
        FrameHelper.repaintComponents(chatIcon);
    }

    private void initNotificationNum(String chatId) {
        Integer numNotification = 0;
        try {
            numNotification = notificationRepository.getNotificationsNum(chatId);
        } catch (Exception ignored) {
        }
        notificationRepository.updateOrCreateNotificationNum(chatId, numNotification);

        ImageIcon num = FrameHelper.getNumber(numNotification, dictionary);
        JLabel chatIcon = chatIconsNotification.computeIfAbsent(chatId, k -> new JLabel(num));
        chatIcon.setIcon(num);
        FrameHelper.repaintComponents(chatIcon);
    }

    private void delNotificationChat() {
        notificationRepository.updateOrCreateNotificationNum(chat.getId(), 0);

        JLabel chatIcon = chatIconsNotification.computeIfAbsent(chat.getId(), k -> new JLabel(dictionary.getEnvelope()));
        chatIcon.setIcon(dictionary.getEnvelope());
        FrameHelper.repaintComponents(chatIcon);
    }

    private final Set<String> usersTyping = new HashSet<>();

    public void addUserTyping(String username, String chatId) {
        if (!chatId.equals(chat.getId())) return;

        usersTyping.add(username);
        userTyping.setText(getTypingText());
    }

    public void delUserTyping(String username, String chatId) {
        if (!chatId.equals(chat.getId())) return;

        usersTyping.remove(username);
        userTyping.setText(getTypingText());
    }

    private String getTypingText() {
        StringBuilder builder = new StringBuilder();

        if (usersTyping.isEmpty()) return "";

        for (String name : usersTyping)
            builder.append(name).append(", ");

        builder.delete(builder.length() - 2, builder.length());
        builder.append(dictionary.getTyping());

        return builder.toString();
    }

    public void findChat(ChatDto chat) {
        userTyping.setText("");
        this.chat = chat;
        chatName.setText(FrameHelper.getChatName(chat));
        chatHistoryCount = 1;
        loadChatHistory();
        delNotificationChat();
    }
}