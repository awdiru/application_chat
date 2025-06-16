package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainFrame extends JFrame implements MessageListener {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Client client;
    private final String username;
    private final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    private JTextArea chatArea;
    private JTextField messageField;
    private JPanel chatsContainer;
    private JPanel invitationsContainer;
    private String chatId;

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;

        client.setMessageListener(this);

        initUi();
        loadChats();
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
                    chatArea.setText("");
                    OffsetDateTime oldDate = get().getFirst().getTime();
                    MainFrameHelper.addDate(oldDate, chatArea, dictionary);

                    for (MessageDto m : get()) {
                        if (m.getTime().toLocalDate().isAfter(oldDate.toLocalDate())) {
                            MainFrameHelper.addDate(m.getTime(), chatArea, dictionary);
                            oldDate = m.getTime();
                        }

                        onMessageReceived(m);
                    }
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());

                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void loadChats() {
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
                    for (InvitationChatDto inv : get()) chatsContainer.add(createInvitationItem(inv));
                    chatsContainer.revalidate();
                    chatsContainer.repaint();
                } catch (Exception e) {
                    MainFrameHelper.errorHandler(e, dictionary, MainFrame.this);
                }
            }
        }.execute();
    }

    private void createChat() {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getAddChatTitle());
        main.setSize(240, 110);
        main.setLocationRelativeTo(null);

        JPanel addChatPanel = new JPanel(new BorderLayout());
        JTextField chatNameField = new JTextField();
        addChatPanel.add(new JLabel(dictionary.getChatName()), BorderLayout.NORTH);
        addChatPanel.add(chatNameField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton pubChatButton = new JButton(dictionary.getPublicChat());
        pubChatButton.addActionListener(e -> {
            try {
                client.createChat(username, chatNameField.getText(), false);
                loadChats();
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
            main.dispose();
        });

        JButton privateChatButton = new JButton(dictionary.getPrivateChat());
        privateChatButton.addActionListener(e -> {
            try {
                client.createChat(username, chatNameField.getText(), true);
                loadChats();
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
            main.dispose();
        });

        buttonPanel.add(pubChatButton, BorderLayout.WEST);
        buttonPanel.add(privateChatButton, BorderLayout.EAST);
        addChatPanel.add(buttonPanel, BorderLayout.SOUTH);

        main.add(addChatPanel);
        main.setVisible(true);
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        if (!message.getChat().equals(chatId)) return;
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
        JScrollPane chatScroll = new JScrollPane(chatArea);
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
        addChatButton.addActionListener(e -> createChat());

        JPanel headerChatsPanel = new JPanel(new BorderLayout());
        headerChatsPanel.add(new JLabel(dictionary.getChats()), BorderLayout.CENTER);
        headerChatsPanel.add(addChatButton, BorderLayout.EAST);

        JPanel chatsPanel = new JPanel(new BorderLayout());
        chatsPanel.add(headerChatsPanel, BorderLayout.NORTH);
        chatsPanel.add(chatsScrollPane, BorderLayout.CENTER);

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
        //invitationsPanel.setMinimumSize(new Dimension(1000, 100));

        return invitationsPanel;
    }

    private JPanel createChatItem(ChatDto chat) {
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    chatId = chat.getId();
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
        JTextArea textArea = new JTextArea(getChatName(chat));
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

    }

    private void addUserFromChat(ChatDto chat) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                addUserFromChatFrame(chat);
                return null;
            }
        }.execute();
    }

    private void addUserFromChatFrame(ChatDto chat) {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getLogoutChat());
        main.setSize(250, 150);
        main.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        main.setLocationRelativeTo(null);
        main.add(getMainWindow());

        String question = dictionary.getAddUser() + " " + getChatName(chat);
        JLabel addLabel = new JLabel("<html><div style='text-align: center;'>" + question + "</div></html>");
        addLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = getAddUserFromChatButtonPanel(chat.getId(), main);

        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.add(addLabel, BorderLayout.NORTH);
        addPanel.add(buttonPanel, BorderLayout.SOUTH);

        main.add(addPanel);
        main.setVisible(true);
    }

    private JPanel getAddUserFromChatButtonPanel(String chatId, JFrame main) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton addButton = new JButton();

        JTextField addUserField = new JTextField();

        addButton.addActionListener(e -> {
            try {
                client.addUserFromChat(addUserField.getText(), chatId);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
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

    private void logoutChat(ChatDto deleteChat) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                logoutChatFrame(deleteChat);
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

    private void logoutChatFrame(ChatDto deleteChat) {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getLogoutChat());
        main.setSize(250, 150);
        main.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        main.setLocationRelativeTo(null);
        main.add(getMainWindow());

        String question = dictionary.getLogoutChatQuestion() + " " + getChatName(deleteChat);
        JLabel deleteLabel = new JLabel("<html><div style='text-align: center;'>" + question + "</div></html>");
        deleteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = getLogoutChatButtonPanel(deleteChat.getId(), main);

        JPanel deletePanel = new JPanel(new BorderLayout());
        deletePanel.add(deleteLabel, BorderLayout.NORTH);
        deletePanel.add(buttonPanel, BorderLayout.SOUTH);

        main.add(deletePanel);
        main.setVisible(true);
    }

    private JPanel getLogoutChatButtonPanel(String deleteChatId, JFrame main) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        JButton yesButton = new JButton();
        yesButton.addActionListener(e -> {
            try {
                client.logoutOfChat(username, deleteChatId);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            } finally {
                main.dispose();
            }
        });
        yesButton.setText(dictionary.getYes());

        JButton noButton = new JButton();
        noButton.addActionListener(e -> main.dispose());
        noButton.setText(dictionary.getNo());

        buttonPanel.add(yesButton, BorderLayout.WEST);
        buttonPanel.add(noButton, BorderLayout.EAST);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        wrapperPanel.add(buttonPanel);
        return wrapperPanel;
    }

    private void renameChat(ChatDto chat, boolean admin) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                renameChatFrame(chat, admin);
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

    private void renameChatFrame(ChatDto renameChat, boolean admin) {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getRenameChatCustom() + " " + getChatName(renameChat));
        main.setSize(250, 150);
        main.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        main.setLocationRelativeTo(null);
        main.add(getMainWindow());

        JTextField renameField = new JTextField();

        JButton renameButton = new JButton();
        renameButton.setText(dictionary.getRename());
        renameButton.addActionListener(e -> {
            try {
                if (admin)
                    client.renameChatAdmin(username, renameChat.getId(), renameField.getText());
                else client.renameChatCustom(username, renameChat.getId(), renameField.getText());
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            } finally {
                main.dispose();
            }
        });

        JPanel renamePanel = new JPanel(new BorderLayout());
        renamePanel.add(renameField, BorderLayout.NORTH);
        renamePanel.add(renameButton, BorderLayout.SOUTH);
        main.add(renamePanel);
        main.setVisible(true);
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
        JButton chatUsers = new JButton(dictionary.getParticipants());
        chatUsers.addActionListener(e -> {
            try {
                getParticipants(chatUsers);
            } catch (Exception ex) {
                MainFrameHelper.errorHandler(ex, dictionary, MainFrame.this);
            }
        });

        JPanel chatStatusBar = new JPanel(new BorderLayout());
        chatStatusBar.add(chatUsers, BorderLayout.EAST);
        return chatStatusBar;
    }

    private void getParticipants(JComponent parent) throws Exception {
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

    private JSplitPane getChatAppSplitPane() {
        //Окно статуса чата
        JPanel chatStatusBar = getChatStatusBar();
        //Окно истории сообщений
        JPanel chatPanel = getChatPanel();
        //Окно ввода текста
        JPanel messagePanel = getMessagePanel();
        //Правая панель
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatStatusBar, BorderLayout.NORTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messagePanel, BorderLayout.SOUTH);
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

    private String getChatName(ChatDto chat) {
        return chat.getCustomName() == null || chat.getCustomName().isEmpty() ? chat.getChatName() : chat.getCustomName() + " (" + chat.getChatName() + ")";
    }
}
