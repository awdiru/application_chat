package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.MessageListener;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.template.exceptions.NoConnectionServerException;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
                    return client.getChatHistory(chatId);
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

    private void loadChats() {
        new SwingWorker<List<ChatDto>, Void>() {
            @Override
            protected List<ChatDto> doInBackground() {
                try {
                    return client.getChats(username);
                } catch (Exception e) {
                    errorHandler(e);
                }
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    chatsContainer.removeAll();
                    for (ChatDto c : get()) {
                        chatsContainer.add(createChatItem(c));
                    }
                    chatsContainer.revalidate();
                    chatsContainer.repaint();
                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void createChat() {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getAddChatTitle());
        main.setSize(300, 70);
        main.setLocationRelativeTo(null);

        JPanel addChatPanel = new JPanel(new GridLayout(1, 1, 3, 3));
        JTextField chatNameField = new JTextField();
        addChatPanel.add(new JLabel(dictionary.getChatName()));
        addChatPanel.add(chatNameField);
        chatNameField.addActionListener(e -> {
            try {
                client.createChat(username, chatNameField.getText());
                loadChats();
            } catch (Exception ex) {
                errorHandler(ex);
            }
            main.dispose();
        });

        main.add(addChatPanel);
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

    private JPanel getChatsPanel() {
        chatsContainer = new JPanel();
        chatsContainer.setLayout(new BoxLayout(chatsContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(chatsContainer);

        JButton addButton = new JButton(dictionary.getPlus());
        addButton.addActionListener(e -> createChat());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(new JLabel(dictionary.getChats()), BorderLayout.CENTER);
        headerPanel.add(addButton, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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

        String chatName = chat.getCustomName() == null || chat.getCustomName().isEmpty()
                ? chat.getChatName() : chat.getCustomName() + "(" + chat.getChatName() + ")";
        JLabel nameLabel = new JLabel(chatName);
        nameLabel.addMouseListener(selectListener);

        JButton menuButton = new JButton(dictionary.getEllipsis());
        menuButton.setSize(new Dimension(10, 10));
        menuButton.setMaximumSize(new Dimension(20, 15));
        menuButton.addActionListener(e -> showChatContextMenu(menuButton, chat));

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(nameLabel, BorderLayout.WEST);
        itemPanel.add(menuButton, BorderLayout.EAST);

        itemPanel.setMaximumSize(new Dimension(10000, 40));
        itemPanel.addMouseListener(selectListener);
        itemPanel.setOpaque(true);

        return itemPanel;
    }

    private void showChatContextMenu(JComponent parent, ChatDto chat) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem renameItem = new JMenuItem(dictionary.getRenameChat());
        renameItem.addActionListener(e -> renameChatCustom(chat));
        menu.add(renameItem);

        JMenuItem removeItem = new JMenuItem(dictionary.getLogoutChat());
        removeItem.addActionListener(e -> logoutChat(chat));
        menu.add(removeItem);

        // TODO Здесь можно добавить другие действия над пользователем

        menu.show(parent, 0, parent.getHeight());
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

        String question = dictionary.getLogoutChatQuestion() + " " + deleteChat.getCustomName();
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
                errorHandler(ex);
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

    private void renameChatCustom(ChatDto chat) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                renameChatCustomFrame(chat);
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

    private void renameChatCustomFrame(ChatDto renameChat) {
        JFrame main = new JFrame();
        main.setTitle(dictionary.getRenameChat() + " " + renameChat.getCustomName());
        main.setSize(250, 150);
        main.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        main.setLocationRelativeTo(null);
        main.add(getMainWindow());

        JTextField renameField = new JTextField();

        JButton renameButton = new JButton();
        renameButton.setText(dictionary.getRename());
        renameButton.addActionListener(e -> {
            try {
                client.renameChatCustom(username, renameChat.getId(), renameField.getText());
            } catch (Exception ex) {
                errorHandler(ex);
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
        restart.addActionListener(e -> {
            dispose();
            MainFrame mainFrame = new MainFrame(client, username);
            client.setMessageListener(mainFrame);
            mainFrame.setVisible(true);
        });
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
        JPanel chatsPanel = getChatsPanel();
        //Левая панель
        JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftPanel.setDividerLocation(400);
        leftPanel.setTopComponent(chatsPanel);

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
