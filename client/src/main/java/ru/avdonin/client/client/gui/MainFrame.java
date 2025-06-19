package ru.avdonin.client.client.gui;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.client.client.additional_frames.AddUserFromChatFrame;
import ru.avdonin.client.client.additional_frames.CreateChatFrame;
import ru.avdonin.client.client.additional_frames.LogoutChatFrame;
import ru.avdonin.client.client.additional_frames.RenameChatFrame;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Getter
public class MainFrame extends JFrame implements MessageListener {
    protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    protected final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    protected final Client client;
    protected final String username;
    protected JTextArea chatArea;
    protected JTextField messageField;
    protected JPanel chatsContainer;
    protected JScrollPane chatScroll;
    protected JPanel invitationsContainer;
    protected String chatId;
    protected int chatHistoryCount = 1;
    protected List<MessageDto> messages = new ArrayList<>();

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;

        client.setMessageListener(this);

        initUi();
        loadChats();
        loadInvitations();
    }

    private void initUi() {
        setTitle(dictionary.getChat() + " - " + username);
        setSize(800, 600);
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

                    client.sendMessage(messageField.getText(), username, chatId);
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
                    return client.getChatHistory(chatId);
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
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
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
                    return client.getChatHistory(chatId, from);
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
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void fillChatArea(List<MessageDto> messages) {
        chatArea.setText("");
        OffsetDateTime oldDate = messages.getFirst().getTime();
        MainFrameHelper.addDate(oldDate, chatArea, dictionary);
        for (MessageDto m : messages) {
            if (m.getTime().toLocalDate().isAfter(oldDate.toLocalDate())) {
                MainFrameHelper.addDate(m.getTime(), chatArea, dictionary);
                oldDate = m.getTime();
            }

            onMessageReceived(m);
        }
    }

    protected void loadChats() {
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
        privateChat.addActionListener(e -> new CreateChatFrame(MainFrame.this,true));
        menu.add(privateChat);

        JMenuItem publicChat = new JMenuItem();
        publicChat.setText(dictionary.getPublicChat());
        publicChat.addActionListener(e -> new CreateChatFrame(MainFrame.this,false));
        menu.add(publicChat);

        menu.show(parent, 0, parent.getHeight());
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        if (!message.getChatId().equals(chatId)) return;
        MainFrameHelper.addTime(message.getTime(), chatArea);
        SwingUtilities.invokeLater(() -> {
            String formatted = String.format("%s: %s\n", message.getSender(), message.getContent());
            chatArea.append(formatted);
        });
    }

    @Override
    public void start() {
        //не используется
    }

    private JPanel getChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(600, 500));
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
        headerChatsPanel.add(new JLabel(dictionary.getChats()), BorderLayout.CENTER);
        headerChatsPanel.add(addChatButton, BorderLayout.EAST);

        JPanel chatsPanel = new JPanel(new BorderLayout());
        chatsPanel.add(headerChatsPanel, BorderLayout.NORTH);
        chatsPanel.add(chatsScrollPane, BorderLayout.CENTER);
        chatsPanel.setMinimumSize(new Dimension(1000, 300));

        return chatsPanel;
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

    private JPanel createChatItem(ChatDto chat) {
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    chatId = chat.getId();
                    chatHistoryCount = 1;
                    loadChatHistory();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setBackground(new Color(183, 250, 211));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setBackground(UIManager.getColor("Panel.background"));
            }
        };
        JTextArea textArea = new JTextArea(MainFrameHelper.getChatName(chat));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        textArea.addMouseListener(selectListener);

        JButton menuButton = new JButton(dictionary.getEllipsis());
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
        JTextArea textArea = new JTextArea(invitation.getChatName());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);

        JButton menuButton = new JButton(dictionary.getEllipsis());
        menuButton.addActionListener(e -> showInvitationsContextMenu(menuButton, invitation));

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(textArea, BorderLayout.CENTER);
        itemPanel.add(menuButton, BorderLayout.EAST);

        itemPanel.setMaximumSize(new Dimension(10000, 40));
        itemPanel.setOpaque(true);

        return itemPanel;
    }

    private void showChatContextMenu(JComponent parent, ChatDto chat) {
        JPopupMenu menu = new JPopupMenu();
        if (!chat.getPrivateChat()) {
            JMenuItem addUserItem = new JMenuItem(dictionary.getAddUser());
            addUserItem.addActionListener(e -> addUserFromChat(chat));
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

    private void addUserFromChat(ChatDto chat) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                new AddUserFromChatFrame(MainFrame.this, chat).setVisible(true);
                return null;
            }
        }.execute();
    }

    private void logoutChat(ChatDto deleteChat) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                new LogoutChatFrame(MainFrame.this, deleteChat).setVisible(true);
                return null;
            }

            @Override
            protected void done() {
                loadChats();
                if (deleteChat.getId().equals(chatId)) {
                    chatArea.setText("");
                }
                chatsContainer.revalidate();
                chatsContainer.repaint();
            }
        }.execute();
    }

    private void renameChat(ChatDto chat, boolean isAdmin) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                new RenameChatFrame(MainFrame.this, chat, isAdmin).setVisible(true);
                return null;
            }

            @Override
            protected void done() {
                loadChats();
                chatsContainer.revalidate();
                chatsContainer.repaint();
            }
        }.execute();
    }

    private JPanel getStatusBar() {
        //Панель кнопок
        JPanel buttonsPanel = new JPanel();
        //Перезагрузить
        JButton restart = new JButton(dictionary.getReboot());
        restart.addActionListener(e -> MainFrameHelper.restart(MainFrame.this, client, username));
        buttonsPanel.add(restart);
        //Настройки
        JButton settings = new JButton(dictionary.getSettings());
        settings.addActionListener(e -> {
            Settings.getFrameSettings();
        });
        buttonsPanel.add(settings);
        //Сменить пользователя
        JButton newUser = new JButton(dictionary.getChangeUser());
        newUser.addActionListener(e -> {
            dispose();
            Client client = new Client();
            new LoginFrame(client).setVisible(true);
        });
        buttonsPanel.add(newUser);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setSize(new Dimension(800, 100));
        statusBar.add(buttonsPanel, BorderLayout.WEST);
        return statusBar;
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

        JPanel chatStatusBar = new JPanel(new BorderLayout());
        chatStatusBar.add(buttonsPanel, BorderLayout.EAST);
        return chatStatusBar;
    }

    private void showContextMenuParticipant(JComponent parent) throws Exception {
        JPopupMenu participants = new JPopupMenu();
        if (chatId == null || chatId.isEmpty()) return;

        List<UserDto> users = client.getChatParticipants(chatId);
        for (UserDto u : users) {
            JMenuItem userItem = new JMenuItem();
            userItem.setText(u.getUsername());
            participants.add(userItem);
            userItem.addActionListener(e -> {
                try {
                    String chatIdNew;
                    if (!u.getUsername().equals(username))
                        chatIdNew = client.getPrivateChat(username, u.getUsername()).getId();
                    else chatIdNew = client.getPersonalChat(username).getId();

                    if (chatId.equals(chatIdNew)) return;

                    chatId = chatIdNew;
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

    protected JSplitPane getMainWindow() {
        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane mainWindow = getChatAppSplitPane();
        JPanel statusBar = getStatusBar();

        main.setTopComponent(statusBar);
        main.setBottomComponent(mainWindow);
        return main;
    }

    protected MainFrame(MainFrame mainFrame) {
        client = mainFrame.client;
        username = mainFrame.username;
        chatArea = mainFrame.chatArea;
        messageField = mainFrame.messageField;
        chatsContainer = mainFrame.chatsContainer;
        chatScroll = mainFrame.chatScroll;
        invitationsContainer = mainFrame.invitationsContainer;
        chatId = mainFrame.chatId;
        chatHistoryCount = mainFrame.chatHistoryCount;
        messages = mainFrame.messages;
    }
}