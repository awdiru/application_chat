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
    private final BaseDictionary dictionary = FactoryLanguage.getFactory().getSettings();
    private JTextArea chatArea;
    private JTextField messageField;
    private DefaultListModel<String> friendsModel;
    private JPanel requestsContainer;
    private String friendName;

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;

        client.setMessageListener(this);

        initUi();
        loadFriends();
        startBackgroundRequestsFriends();
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
                    if (!client.isConnected()) {
                        client.connect(username);
                        if (!client.isConnected())
                            throw new NoConnectionServerException("There is no connection to the server");
                    }

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
                    requestsContainer.removeAll();
                    for (FriendDto request : get()) {
                        requestsContainer.add(createRequestItem(request));
                    }
                    // Обновляем UI
                    requestsContainer.revalidate();
                    requestsContainer.repaint();
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
        main.setTitle(dictionary.getAddFriendTitle());
        main.setSize(300, 70);
        main.setLocationRelativeTo(null);

        JPanel addFriendPanel = new JPanel(new GridLayout(1, 1, 3, 3));
        JTextField friendField = new JTextField();
        addFriendPanel.add(new JLabel(dictionary.getFriendName()));
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

    private void rmFriend() {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getRmFriendTitle());
        main.setSize(300, 70);
        main.setLocationRelativeTo(null);

        JPanel rmFriendPanel = new JPanel(new GridLayout(1, 1, 3, 3));
        JTextField friendField = new JTextField();
        rmFriendPanel.add(new JLabel(dictionary.getFriendName()));
        rmFriendPanel.add(friendField);
        friendField.addActionListener(e -> {
            try {
                client.rmFriend(username, friendField.getText());
                loadFriends();
            } catch (Exception ex) {
                errorHandler(ex);
            }
            main.dispose();
        });

        main.add(rmFriendPanel);
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
            case JANUARY -> dictionary.getJanuary();
            case FEBRUARY -> dictionary.getFebruary();
            case MARCH -> dictionary.getMarch();
            case APRIL -> dictionary.getApril();
            case MAY -> dictionary.getMay();
            case JUNE -> dictionary.getJune();
            case JULY -> dictionary.getJuly();
            case AUGUST -> dictionary.getAugust();
            case SEPTEMBER -> dictionary.getSeptember();
            case OCTOBER -> dictionary.getOctober();
            case NOVEMBER -> dictionary.getNovember();
            default -> dictionary.getDecember();
        };
    }

    private String getDayOfWeek(OffsetDateTime date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> dictionary.getMonday();
            case TUESDAY -> dictionary.getTuesday();
            case WEDNESDAY -> dictionary.getWednesday();
            case THURSDAY -> dictionary.getThursday();
            case FRIDAY -> dictionary.getFriday();
            case SATURDAY -> dictionary.getSaturday();
            default -> dictionary.getSunday();
        };
    }

    private void errorHandler(Exception e) {
        if (e.getMessage() == null || e.getMessage().isEmpty()) return;
        JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), dictionary.getError(), JOptionPane.ERROR_MESSAGE);
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

    private JPanel getFriendPanel() {
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

        JButton addFriend = new JButton(dictionary.getPlus());
        addFriend.addActionListener(e -> addFriend());

        JButton rmFriend = new JButton(dictionary.getMinus());
        rmFriend.addActionListener(e -> rmFriend());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addFriend);
        buttonPanel.add(rmFriend);

        JPanel friendsLabel = new JPanel(new BorderLayout());
        friendsLabel.add(new JLabel(dictionary.getFriends()), BorderLayout.CENTER);
        friendsLabel.add(buttonPanel, BorderLayout.EAST);

        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsPanel.add(friendsLabel, BorderLayout.NORTH);
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        return friendsPanel;
    }

    private JPanel getRequestsFriendsPanel() {
        JPanel requestFriendsPanel = new JPanel(new BorderLayout());
        requestFriendsPanel.add(new JLabel(dictionary.getRequestFriends()), BorderLayout.NORTH);
        // Создаем контейнер с вертикальным расположением
        requestsContainer = new JPanel();
        requestsContainer.setLayout(new BoxLayout(requestsContainer, BoxLayout.Y_AXIS));
        // Добавляем скроллинг
        JScrollPane scrollPane = new JScrollPane(requestsContainer);
        requestFriendsPanel.add(scrollPane, BorderLayout.CENTER);

        return requestFriendsPanel;
    }

    private JPanel createRequestItem(FriendDto friendRequest) {
        JPanel label = new JPanel(new FlowLayout());
        label.add(new JLabel(friendRequest.getUsername()), FlowLayout.LEFT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton acceptButton = new JButton("+");
        acceptButton.setToolTipText(dictionary.getConfirmFriend());
        acceptButton.addActionListener(e -> handleFriendResponse(friendRequest.getUsername(), true));

        JButton rejectButton = new JButton("-");
        rejectButton.setToolTipText(dictionary.getRejectedFriend());
        rejectButton.addActionListener(e -> handleFriendResponse(friendRequest.getUsername(), false));

        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(label, BorderLayout.WEST);
        itemPanel.add(buttonPanel, BorderLayout.EAST);

        return itemPanel;
    }

    private void handleFriendResponse(String friendName, boolean accept) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    client.confirmFriend(username, friendName, accept);
                } catch (Exception ex) {
                    errorHandler(ex);
                }
                return null;
            }

            @Override
            protected void done() {
                loadRequestsFriends();
                loadFriends();
            }
        }.execute();
    }

    private JPanel getStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setSize(new Dimension(800, 100));
        //Панель кнопок
        JPanel buttonsPanel = new JPanel();
        //Перезагрузить
        JButton restart = new JButton(FactoryLanguage.getFactory().getSettings().getRestart());
        restart.addActionListener(e -> {
            dispose();
            MainFrame mainFrame = new MainFrame(client, username);
            client.setMessageListener(mainFrame);
            mainFrame.setVisible(true);
        });
        buttonsPanel.add(restart);
        //Сменить пользователя
        JButton newUser = new JButton(dictionary.getChangeUser());
        newUser.addActionListener(e -> {
            dispose();
            stopBackgroundRequestsFriends();
            Client client = new Client();
            new LoginFrame(client).setVisible(true);
        });
        buttonsPanel.add(newUser);
        //Настройки
        JButton settings = new JButton(dictionary.getSettings());
        settings.addActionListener(e -> {
            Settings.getFrameSettings();
        });
        buttonsPanel.add(settings);
        statusBar.add(buttonsPanel, BorderLayout.WEST);

        return statusBar;
    }

    private JSplitPane getChatAppSplitPane() {
        //Окно истории сообщений
        JPanel chatPanel = getChatPanel();
        //Окно ввода текста
        JPanel messagePanel = getMessagePanel();
        //Правая панель
        JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightPanel.setDividerLocation(450);
        rightPanel.setTopComponent(chatPanel);
        rightPanel.setBottomComponent(messagePanel);
        //Панель друзей
        JPanel friendsPanel = getFriendPanel();
        //панель запросов в друзья
        JPanel requestFriendsPanel = getRequestsFriendsPanel();
        //Левая панель
        JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftPanel.setDividerLocation(400);
        leftPanel.setTopComponent(friendsPanel);
        leftPanel.setBottomComponent(requestFriendsPanel);

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
