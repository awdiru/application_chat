package ru.avdonin.client.client.gui;

import jakarta.websocket.DeploymentException;
import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.ConstatntsGUI.ConstantsGUI;
import ru.avdonin.client.client.gui.additional.frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.additional.panels.MessageAreaJPanel;
import ru.avdonin.client.client.gui.additional.panels.MessageJPanel;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

@Getter
public class MainFrame extends JFrame {
    private final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    private final Map<String, ImageIcon> avatars = new HashMap<>();
    private final Map<String, JLabel> chatIconsNotification = new HashMap<>();
    private final Map<String, Integer> chatNotifications = new HashMap<>();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final Client client;
    private final String username;

    private Integer chatHistoryCount = 1;

    private MessageAreaJPanel messageArea;

    private JPanel chatArea;
    private JPanel invitationsContainer;
    private JPanel chatsContainer;

    private JTextArea chatName;

    private JScrollPane chatScroll;

    private ChatDto chat;

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

        chatArea.add(new MessageJPanel(MainFrame.this, message));

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
                        chatArea.add(new MessageJPanel(MainFrame.this, m));

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

                    List<JPanel> newMessages = new ArrayList<>();
                    int totalHeight = 0;

                    for (MessageDto m : get()) {
                        JPanel messageItem = new MessageJPanel(MainFrame.this, m);
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
        chatArea.setBackground((Color) ConstantsGUI.BACKGROUND_COLOR.getValue());

        chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(500, 500));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        return chatPanel;
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
                e.getComponent().setBackground((Color) ConstantsGUI.BACKGROUND_COLOR.getValue());
            }
        };
        JTextArea textArea = FrameHelper.getTextArea(FrameHelper.getChatName(chat), selectListener);

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
                e.getComponent().setBackground((Color) ConstantsGUI.BACKGROUND_COLOR.getValue());
            }
        };

        JTextArea textArea = FrameHelper.getTextArea(invitation.getChatName(), selectListener);

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

    public void loadAvatarAsync(String username, JComponent component) {
        SwingUtilities.invokeLater(() -> {
            try {
                String avatarBase64 = client.getAvatar(username);
                ImageIcon avatar = FrameHelper.getScaledIcon(avatarBase64,
                        (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                        (Integer) Constants.COMPRESSION_AVATAR.getValue(),
                        dictionary);
                avatars.get(username).setImage(avatar.getImage());
                FrameHelper.repaintComponents(component);
            } catch (Exception e) {
                FrameHelper.errorHandler(e, dictionary, MainFrame.this);
            }
        });
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
                messageArea.getSentImagesBase64().clear();
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

        chatName = FrameHelper.getTextArea(FrameHelper.getChatName(chat), null);

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
        messageArea = new MessageAreaJPanel(MainFrame.this);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatStatusBar, BorderLayout.NORTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messageArea, BorderLayout.SOUTH);

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

    public void connect() throws DeploymentException, IOException {
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

    public void findChat(ChatDto chat) {
        messageArea.clear();
        this.chat = chat;
        chatName.setText(FrameHelper.getChatName(chat));
        chatHistoryCount = 1;
        loadChatHistory();
        delNotificationChat();
    }
}