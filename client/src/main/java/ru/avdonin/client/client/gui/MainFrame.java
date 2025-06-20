package ru.avdonin.client.client.gui;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.GUI;
import ru.avdonin.client.client.gui.additional_frames.AdditionalFrameFactory;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.chat.dto.InvitationChatDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class MainFrame extends JFrame implements GUI {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    private final Client client;
    private final String username;
    private JPanel chatArea;
    private JTextField messageField;
    private JPanel chatsContainer;
    private JScrollPane chatScroll;
    private JPanel invitationsContainer;
    private ChatDto chat;
    private Integer chatHistoryCount = 1;
    private List<MessageDto> messages = new ArrayList<>();
    private JTextArea chatName = new JTextArea();

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;

        client.setGui(this);

        initUi();
        loadChats();
        loadInvitations();
        setVisible(true);
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        if (!message.getChatId().equals(chat.getId())) return;

        chatArea.add(createMessageItem(message));

        JScrollBar vertical = chatScroll.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

        chatArea.revalidate();
        chatArea.repaint();
    }

    @Override
    public void loadChats() {
        new SwingWorker<List<ChatDto>, Void>() {
            @Override
            protected List<ChatDto> doInBackground() {
                try {
                    return client.getChats(username);
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    chatsContainer.removeAll();
                    for (ChatDto c : get()) chatsContainer.add(createChatItem(c));
                    chatsContainer.revalidate();
                    chatsContainer.repaint();
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
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
                    if (client.isNotConnected()) {
                        client.connect(username);
                        if (client.isNotConnected())
                            throw new NoConnectionServerException("There is no connection to the server");
                    }

                    client.sendMessage(messageField.getText(), username, chat.getId());
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return null;
            }

            @Override
            protected void done() {
                messageField.setText("");
            }
        }.execute();
    }

    private void loadChatHistory() {
        new SwingWorker<List<MessageDto>, Void>() {
            @Override
            protected List<MessageDto> doInBackground() {
                try {
                    return client.getChatHistory(chat.getId());
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    fillChatArea(get());
                    messages = new ArrayList<>(get());
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
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
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    if (get().isEmpty()) return;
                    chatHistoryCount++;
                    messages.addAll(get());
                    messages = new ArrayList<>(messages.stream()
                            .sorted(Comparator.comparing(MessageDto::getTime))
                            .toList());
                    fillChatArea(messages);
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void fillChatArea(List<MessageDto> messages) {
        chatArea.removeAll();
        for (MessageDto m : messages) {
            onMessageReceived(m);
        }
    }

    private void loadInvitations() {
        new SwingWorker<List<InvitationChatDto>, Void>() {
            @Override
            protected List<InvitationChatDto> doInBackground() {
                try {
                    return client.getInvitationsChats(username);
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    invitationsContainer.removeAll();
                    for (InvitationChatDto inv : get()) invitationsContainer.add(createInvitationItem(inv));
                    invitationsContainer.revalidate();
                    invitationsContainer.repaint();
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
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

        chatArea.add(Box.createVerticalGlue());

        chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(500, 500));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        return chatPanel;
    }

    private JPanel getMessagePanel() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        return messagePanel;
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

        JButton addChatButton = new JButton(dictionary.getPlus());
        addChatButton.addActionListener(e -> showCreateChatContextMenu(addChatButton));

        JPanel headerChatsPanel = new JPanel(new BorderLayout());

        ImageIcon avatarIcon = getAvatarIcon();

        String spase;
        if (avatarIcon != null) {
            headerChatsPanel.add(new JLabel(avatarIcon), BorderLayout.WEST);
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
            if (userDto != null && userDto.getAvatarBase64() != null
                    && !userDto.getAvatarBase64().isEmpty()) {

                byte[] imageData = Base64.getDecoder().decode(userDto.getAvatarBase64());
                ImageIcon avatarIcon = new ImageIcon(imageData);
                Image scaledImage = avatarIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
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
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
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
                    MainFrame.this.chat = chat;
                    chatName.setText(MainFrameHelper.getChatName(chat));
                    chatHistoryCount = 1;
                    loadChatHistory();
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
        JTextArea textArea = getTextArea(MainFrameHelper.getChatName(chat), selectListener);

        JButton menuButton = new JButton(dictionary.getBurger());
        menuButton.addActionListener(e -> showChatContextMenu(menuButton, chat));

        JPanel itemPanel = new JPanel(new BorderLayout());
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

        ImageIcon avatarIcon = null;
        if (messageDto.getAvatarBase64() != null && !messageDto.getAvatarBase64().isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(messageDto.getAvatarBase64());
                avatarIcon = new ImageIcon(imageData);
                Image scaledImage = avatarIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                avatarIcon = new ImageIcon(scaledImage);
            } catch (Exception e) {
            }
        }
        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);

        if (avatarIcon != null) {
            JLabel avatarLabel = new JLabel(avatarIcon);
            avatarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            headerPanel.add(avatarLabel, BorderLayout.WEST);
        }

        JTextPane title = getTextPane();

        String formattedTitle = "<html><div style='padding:2px'>"
                + MainFrameHelper.formatTime(messageDto.getTime())
                + " <b>" + messageDto.getSender() + "</b></div></html>";
        title.setText(formattedTitle);
        headerPanel.add(title, BorderLayout.CENTER);

        JTextPane content = getTextPane();
        content.setText(messageDto.getTextContent());
        content.setSize(new Dimension(250, Short.MAX_VALUE));
        int height = content.getPreferredSize().height;

        JPanel message = new JPanel(new BorderLayout());
        message.add(headerPanel, BorderLayout.NORTH);
        message.add(content, BorderLayout.CENTER);
        message.setBorder(new EmptyBorder(5, 5, 5, 5));
        message.setPreferredSize(new Dimension(250, height + 50));

        Color bgColor = messageDto.getSender().equals(username) ? selfMessage : friendMessage;
        message.setBackground(bgColor);
        title.setBackground(bgColor);
        content.setBackground(bgColor);

        JPanel container = new JPanel();
        container.setBackground(backgroungColor);
        container.setOpaque(false);

        if (messageDto.getSender().equals(username))
            container.setLayout(new FlowLayout(FlowLayout.RIGHT));
        else container.setLayout(new FlowLayout(FlowLayout.LEFT));
        container.add(message);
        return container;
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
            JMenuItem addUserItem = new JMenuItem(dictionary.getAddUser());
            addUserItem.addActionListener(e -> AdditionalFrameFactory.getAddUserFromChatFrame(MainFrame.this, chat));
            menu.add(addUserItem);
        }
        if (chat.getAdmin().equals(username) && !chat.getPrivateChat()) {
            JMenuItem renameItemAdmin = new JMenuItem(dictionary.getRenameChatAdmin());
            renameItemAdmin.addActionListener(e -> renameChat(chat, true));
            menu.add(renameItemAdmin);
        }

        JMenuItem renameItemCustom = new JMenuItem();
        if (chat.getPrivateChat())
            renameItemCustom.setText(dictionary.getRename());
        else renameItemCustom.setText(dictionary.getRenameChatCustom());
        renameItemCustom.addActionListener(e -> renameChat(chat, false));
        menu.add(renameItemCustom);

        JMenuItem removeItem = new JMenuItem();
        if (chat.getPrivateChat())
            removeItem.setText(dictionary.getDeleteChat());
        else removeItem.setText(dictionary.getLogoutChat());
        removeItem.addActionListener(e -> logoutChat(chat));
        menu.add(removeItem);

        // TODO Здесь можно добавить другие действия над пользователем

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
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
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
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        menu.add(rejectInvite);

        menu.show(parent, 0, parent.getHeight());
    }

    private void logoutChat(ChatDto deleteChat) {
        AdditionalFrameFactory.getLogoutChatFrame(MainFrame.this, deleteChat);
        if (chat != null && deleteChat.getId().equals(chat.getId()))
            chatArea.removeAll();
        chatsContainer.revalidate();
        chatsContainer.repaint();
    }

    private void renameChat(ChatDto chat, boolean isAdmin) {
        AdditionalFrameFactory.getRenameChatFrame(MainFrame.this, chat, isAdmin);
        chatsContainer.revalidate();
        chatsContainer.repaint();
    }

    private JPanel getStatusBar() {
        //Панель кнопок
        JPanel buttonsPanel = new JPanel();
        //Перезагрузить
        JButton restart = new JButton(dictionary.getReboot());
        restart.addActionListener(e -> MainFrameHelper.restart(MainFrame.this, client, username));
        buttonsPanel.add(restart);
        //Сменить аватар
        JButton changeAvatarButton = new JButton(dictionary.getChangeAvatar());
        changeAvatarButton.addActionListener(e -> changeAvatar());
        buttonsPanel.add(changeAvatarButton);
        //Настройки
        JButton settings = new JButton(dictionary.getSettings());
        settings.addActionListener(e -> Settings.getFrameSettings());
        buttonsPanel.add(settings);
        //Сменить пользователя
        JButton newUser = new JButton(dictionary.getChangeUser());
        newUser.addActionListener(e -> {
            try {
                dispose();
                client.disconnect();
                new LoginFrame(client);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(newUser);

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
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        }
    }

    private JPanel getChatStatusBar() {
        JPanel buttonsPanel = new JPanel();

        JButton messagesButton = new JButton(dictionary.getUpArrow());
        messagesButton.addActionListener(e -> {
            try {
                loadChatHistory(chatHistoryCount);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(messagesButton);

        JButton chatUsersButton = new JButton(dictionary.getParticipants());
        chatUsersButton.addActionListener(e -> {
            try {
                showContextMenuParticipant(chatUsersButton);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });
        buttonsPanel.add(chatUsersButton);

        chatName = getTextArea(MainFrameHelper.getChatName(chat), null);

        JPanel chatStatusBar = new JPanel(new BorderLayout());
        chatStatusBar.add(chatName, BorderLayout.WEST);
        chatStatusBar.add(buttonsPanel, BorderLayout.EAST);
        return chatStatusBar;
    }

    private void showContextMenuParticipant(JComponent parent) throws Exception {
        JPopupMenu participants = new JPopupMenu();
        if (chat == null || chat.getId().isEmpty()) return;

        List<UserDto> users = client.getChatParticipants(chat.getId());
        for (UserDto u : users) {
            JMenuItem userItem = new JMenuItem();
            userItem.setText(u.getUsername());
            participants.add(userItem);
            userItem.addActionListener(e -> {
                try {
                    ChatDto newChat;
                    if (!u.getUsername().equals(username))
                        newChat = client.getPrivateChat(username, u.getUsername());
                    else newChat = client.getPersonalChat(username);

                    if (chat.equals(newChat)) return;

                    chat = newChat;
                    chatName.setText(MainFrameHelper.getChatName(chat));
                    loadChatHistory();
                } catch (Exception ex) {
                    MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
                }
            });
        }
        participants.show(parent, 0, parent.getHeight());
    }

    private JPanel getRightPanel() {
        //Панель статуса чата
        JPanel chatStatusBar = getChatStatusBar();
        //Панель истории сообщений
        JPanel chatPanel = getChatPanel();
        //Панель ввода текста
        JPanel messagePanel = getMessagePanel();

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatStatusBar, BorderLayout.NORTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messagePanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JSplitPane getChatAppSplitPane() {
        //Правая панель
        JPanel rightPanel = getRightPanel();
        //Левая панель
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
}