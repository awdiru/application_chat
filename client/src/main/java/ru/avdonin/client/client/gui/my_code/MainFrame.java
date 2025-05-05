package ru.avdonin.client.client.gui.my_code;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.client.settings.language.LanguageProcessor;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame implements MessageListener {
    private final Client client;
    private final String username;
    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> friendsList;
    private JList<String> requestFriendsList;
    private String friendName;

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        client.setMessageListener(this);
        initUi();
    }

    private void initUi() {
        setTitle(LanguageProcessor.chat() + " - " + username);
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

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(messagePanel, BorderLayout.SOUTH);

        //Панель друзей
        JPanel friendsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = friendsList.getSelectedValue();
                if (selected != null) {
                    friendName = selected;
                    loadChatHistory();
                }
            }
        });

        friendsPanel.add(new JLabel(LanguageProcessor.friends()), BorderLayout.NORTH);
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        //панель запросов в друзья
        JPanel requestFriendsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> requestFriendsModel = new DefaultListModel<>();
        requestFriendsList = new JList<>(requestFriendsModel);
        requestFriendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        requestFriendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = requestFriendsList.getSelectedValue();
                if (selected != null)
                    confirmFriend();
            }
        });

        requestFriendsPanel.add(new JLabel(LanguageProcessor.requestFriends()), BorderLayout.NORTH);
        requestFriendsPanel.add(new JScrollPane(requestFriendsList), BorderLayout.CENTER);

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftPane.setDividerLocation(400);
        leftPane.setTopComponent(friendsPanel);
        leftPane.setBottomComponent(requestFriendsPanel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(leftPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane);
    }

    private void loadFriends() {
        new SwingWorker<List<FriendDto>, Void>() {
            @Override
            protected List<FriendDto> doInBackground() throws Exception {

                return List.of();
            }
        }.execute();
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
            protected List<MessageDto> doInBackground() throws Exception {
                try {
                    return client.getChat(username, friendName);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    chatArea.setText("");
                    LocalDateTime oldDate = get().getFirst().getTime();
                    addDate(oldDate);

                    for (MessageDto m : get()) {
                        if (m.getTime().toLocalDate().isBefore(oldDate.toLocalDate()))
                            addDate(m.getTime());

                        onMessageReceived(m);
                    }
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());

                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void confirmFriend() {
        new SwingWorker<Void, Void>(){

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    client.confirmFriend(username, friendName, true);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return null;
            }

            @Override
            protected void done() {
                try {

                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    @Override
    public void onMessageReceived(MessageDto message) {
        addTime(message.getTime());
        SwingUtilities.invokeLater(() -> {
            String formatted = String.format("%s: %s\n",
                    message.getSender(),
                    message.getContent());
            chatArea.append(formatted);
        });
    }

    private void addDate(LocalDateTime dateTime) {
        SwingUtilities.invokeLater(() -> {
            String date = getDayOfWeek(dateTime) + ", "
                    + dateTime.getDayOfMonth() + " "
                    + getMonth(dateTime) + "\n";
            chatArea.append(date);
        });
    }

    private void addTime(LocalDateTime dateTime) {
        SwingUtilities.invokeLater(() -> {
            String time = dateTime.format(DateTimeFormatter.ofPattern("hh:mm "));
            chatArea.append(time);
        });
    }

    private String getMonth(LocalDateTime date) {
        return switch (date.getMonth()) {
            case JANUARY -> "января";
            case FEBRUARY -> "февраля";
            case MARCH -> "марта";
            case APRIL -> "апреля";
            case MAY -> "мая";
            case JUNE -> "июня";
            case JULY -> "июля";
            case AUGUST -> "августа";
            case SEPTEMBER -> "сентября";
            case OCTOBER -> "октября";
            case NOVEMBER -> "ноября";
            default -> "декабря";
        };
    }

    private String getDayOfWeek(LocalDateTime date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Пн";
            case TUESDAY -> "Вт";
            case WEDNESDAY -> "Ср";
            case THURSDAY -> "Чт";
            case FRIDAY -> "Пт";
            case SATURDAY -> "Сб";
            default -> "Вс";
        };
    }

    private void errorHandler(Exception e) {
        JOptionPane.showMessageDialog(MainFrame.this,
                e.getMessage(), LanguageProcessor.error(),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void start() {
        //не используется
    }
}
