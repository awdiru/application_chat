package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFrame extends JFrame implements MessageListener {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Client client;
    private final String username;
    private final BaseDictionary language;
    private JTextArea chatArea;
    private JTextField messageField;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> requestFriendsModel;
    private String friendName;

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        this.language = FactoryLanguage.getFactory().getSettings();

        client.setMessageListener(this);

        initUi();
        loadFriends();
        startBackgroundRequestsFriends();
    }

    private void initUi() {
        setTitle(language.getChat() + " - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(200);

        //Окно истории сообщений
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(600, 500));
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        //Окно ввода текста
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);

        //Правая панель
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messagePanel, BorderLayout.SOUTH);

        //Панель друзей
        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsModel = new DefaultListModel<>();
        JList<String> friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = friendsList.getSelectedValue();
                if (selected != null) {
                    friendName = selected.split(" ")[0];
                    loadChatHistory();
                }
            }
        });
        JPanel friendsLabel = new JPanel(new BorderLayout());
        JButton addFriend = new JButton(language.getAddFriendBtn());
        friendsLabel.add(new JLabel(language.getFriends()), BorderLayout.CENTER);
        friendsLabel.add(addFriend, BorderLayout.EAST);

        addFriend.addActionListener(e -> addFriend());

        friendsPanel.add(friendsLabel, BorderLayout.NORTH);
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        //панель запросов в друзья
        JPanel requestFriendsPanel = new JPanel(new BorderLayout());
        requestFriendsModel = new DefaultListModel<>();
        JList<String> requestFriendsList = new JList<>(requestFriendsModel);
        requestFriendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        requestFriendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = requestFriendsList.getSelectedValue();
                if (selected != null) confirmFriend(selected);
            }
        });

        requestFriendsPanel.add(new JLabel(language.getRequestFriends()), BorderLayout.NORTH);
        requestFriendsPanel.add(new JScrollPane(requestFriendsList), BorderLayout.CENTER);

        //Левая панель
        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftPane.setDividerLocation(400);
        leftPane.setTopComponent(friendsPanel);
        leftPane.setBottomComponent(requestFriendsPanel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(leftPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        //Строка состояния
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setSize(new Dimension(800, 100));
        //Панель кнопок
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        //Настройки
        JButton settings = new JButton(language.getSettingsIcon());
        settings.addActionListener(e -> {
            Settings.getFrame();
        });
        buttonsPanel.add(settings, BorderLayout.CENTER);

        //Сменить пользователя
        JButton newUser = new JButton(language.getChangeUser());
        newUser.addActionListener(e -> {
            dispose();
            stopBackgroundRequestsFriends();
            Client client = new Client();
            new LoginFrame(client).setVisible(true);
        });
        buttonsPanel.add(newUser, BorderLayout.WEST);

        statusBar.add(buttonsPanel, BorderLayout.WEST);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        main.setTopComponent(statusBar);
        main.setBottomComponent(splitPane);

        add(main);
    }

    private void sendMessage() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (!client.isConnected())
                        throw new NoConnectionServerException("There is no connection to the server");

                    client.sendMessage(messageField.getText(), username, friendName);
                } catch (Exception e) {
                    errorHandler(e);
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
                    return client.getChat(username, friendName);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    chatArea.setText("");
                    OffsetDateTime oldDate = get().getFirst().getTime();
                    addDate(oldDate);

                    for (MessageDto m : get()) {
                        if (m.getTime().toLocalDate().isAfter(oldDate.toLocalDate())) {
                            addDate(m.getTime());
                            oldDate = m.getTime();
                        }

                        onMessageReceived(m);
                    }
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());

                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void loadFriends() {
        new SwingWorker<List<FriendDto>, Void>() {
            @Override
            protected List<FriendDto> doInBackground() {
                try {
                    return client.getFriends(username);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    friendsModel.clear();
                    for (FriendDto f : get())
                        friendsModel.addElement(f.getCustomFriendName() + " " + f.getConfirmation().getIcon());
                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void loadRequestsFriends() {
        new SwingWorker<List<FriendDto>, Void>() {

            @Override
            protected List<FriendDto> doInBackground() {
                try {
                    return client.getRequestsFriends(username);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    requestFriendsModel.clear();
                    for (FriendDto f : get()) requestFriendsModel.addElement(f.getUsername());
                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void startBackgroundRequestsFriends() {
        scheduler.scheduleAtFixedRate(() -> {
                    try {
                        loadRequestsFriends();
                    } catch (Exception e) {
                        errorHandler(e);
                    }
                }, 0, 20, TimeUnit.SECONDS
        );
    }

    private void stopBackgroundRequestsFriends() {
        scheduler.shutdown();
    }

    private void addFriend() {
        JFrame main = new JFrame();
        main.setTitle(language.getAddFriendTitle());
        main.setSize(300, 70);
        main.setLocationRelativeTo(null);

        JPanel addFriendPanel = new JPanel(new GridLayout(1, 1, 3, 3));
        JTextField friendField = new JTextField();
        addFriendPanel.add(new JLabel(language.getFriendName()));
        addFriendPanel.add(friendField);
        friendField.addActionListener(e -> {
            try {
                client.addFriend(username, friendField.getText());
                loadFriends();
            } catch (Exception ex) {
                errorHandler(ex);
            }
            main.dispose();
        });

        main.add(addFriendPanel);
        main.setVisible(true);
    }

    private void confirmFriend(String friend) {
        JFrame main = new JFrame();
        main.setTitle(language.getConfirmFriendTitle());
        main.setSize(200, 150);
        main.setLocationRelativeTo(null);

        JPanel confirmFriendPanel = new JPanel(new GridLayout(1, 1, 1, 1));
        JButton confirmBtn = new JButton(language.getConfirmFriend());
        JButton rejectBtn = new JButton(language.getRejectedFriend());
        confirmFriendPanel.add(confirmBtn);
        confirmFriendPanel.add(rejectBtn);

        confirmBtn.addActionListener(e -> {
            try {
                client.confirmFriend(username, friend, true);
                loadRequestsFriends();
                loadFriends();
            } catch (Exception ex) {
                errorHandler(ex);
            }
            main.dispose();
        });
        rejectBtn.addActionListener(e -> {
            try {
                client.confirmFriend(username, friend, false);
                loadRequestsFriends();
                loadFriends();
            } catch (Exception ex) {
                errorHandler(ex);
            }
            main.dispose();
        });

        main.add(confirmFriendPanel);
        main.setVisible(true);
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        addTime(message.getTime());
        SwingUtilities.invokeLater(() -> {
            String formatted = String.format("%s: %s\n", message.getSender(), message.getContent());
            chatArea.append(formatted);
        });
    }

    private void addDate(OffsetDateTime dateTime) {
        SwingUtilities.invokeLater(() -> {
            String date = getDayOfWeek(dateTime) + ", " + dateTime.getDayOfMonth() + " " + getMonth(dateTime) + "\n";
            chatArea.append(date);
        });
    }

    private void addTime(OffsetDateTime dateTime) {
        SwingUtilities.invokeLater(() -> {
            String time = dateTime.format(DateTimeFormatter.ofPattern("hh:mm "));
            chatArea.append(time);
        });
    }

    private String getMonth(OffsetDateTime date) {
        return switch (date.getMonth()) {
            case JANUARY -> language.getJanuary();
            case FEBRUARY -> language.getFebruary();
            case MARCH -> language.getMarch();
            case APRIL -> language.getApril();
            case MAY -> language.getMay();
            case JUNE -> language.getJune();
            case JULY -> language.getJuly();
            case AUGUST -> language.getAugust();
            case SEPTEMBER -> language.getSeptember();
            case OCTOBER -> language.getOctober();
            case NOVEMBER -> language.getNovember();
            default -> language.getDecember();
        };
    }

    private String getDayOfWeek(OffsetDateTime date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> language.getMonday();
            case TUESDAY -> language.getTuesday();
            case WEDNESDAY -> language.getWednesday();
            case THURSDAY -> language.getThursday();
            case FRIDAY -> language.getFriday();
            case SATURDAY -> language.getSaturday();
            default -> language.getSunday();
        };
    }

    private void errorHandler(Exception e) {
        if (e.getMessage() == null || e.getMessage().isEmpty()) return;
        JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), language.getError(), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void start() {
        //не используется
    }
}
