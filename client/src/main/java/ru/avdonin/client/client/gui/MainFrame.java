package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.client.model.message.MessageDto;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame implements MessageListener {
    private final Client client;
    private final String username;
    private JTextArea chatArea;
    private JTextField recipientField;
    private JTextField messageField;

    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        client.setMessageListener(this);
        initUI();
    }

    private void initUI() {
        setTitle("Чат - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // История сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель ввода
        JPanel inputPanel = new JPanel(new BorderLayout());

        recipientField = new JTextField();
        recipientField.setPreferredSize(new Dimension(150, 30));
        inputPanel.add(recipientField, BorderLayout.WEST);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messagePanel.add(messageField, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Отправить");
        messagePanel.add(sendBtn, BorderLayout.EAST);

        inputPanel.add(messagePanel, BorderLayout.CENTER);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Обработчики событий
        sendBtn.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        recipientField.addActionListener(e -> loadChatHistory());

        add(mainPanel);
    }

    private void sendMessage() {
        String content = messageField.getText();
        String recipient = recipientField.getText();

        if (content.isEmpty() || recipient.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля");
            return;
        }

        if (!client.isConnected()) {
            JOptionPane.showMessageDialog(this, "Нет соединения с сервером");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    client.sendMessage(content, username, recipient);
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Ошибка отправки: " + e.getMessage()));
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
                return client.getChat(username, recipientField.getText());
            }

            @Override
            protected void done() {
                try {
                    chatArea.setText(""); // Очищаем перед загрузкой новых сообщений
                    LocalDateTime oldTime = get().getFirst().getTime();
                    addDate(oldTime);
                    for (MessageDto m : get()) {
                        if (m.getTime().toLocalDate().isAfter(oldTime.toLocalDate()))
                            addDate(m.getTime());
                        addTime(m.getTime());
                        onMessageReceived(m);
                    }
                    // Автоматическая прокрутка вниз
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    public void onMessageReceived(MessageDto message) {
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

    @Override
    public void start() {
        // Не используется в GUI
    }
}
