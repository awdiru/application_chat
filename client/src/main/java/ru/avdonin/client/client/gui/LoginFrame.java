package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private Client client;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(Client client) {
        this.client = client;
        initUi();
    }

    private void initUi() {
        setTitle("Авторизация");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        JButton loginBtn = new JButton("Войти");
        JButton registerBtn = new JButton("Зарегистрироваться");

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Имя пользователя:"));
        panel.add(usernameField);
        panel.add(new JLabel("Пароль:"));
        panel.add(passwordField);

        panel.add(loginBtn);
        panel.add(registerBtn);

        loginBtn.addActionListener(e -> tryLogin("/login"));
        registerBtn.addActionListener(e -> tryLogin("/signup"));

        add(panel);
    }

    private void tryLogin(String path) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.login(username, password, path);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        dispose();
                        client.connect(username);
                        new MainFrame(client, username).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Ошибка аутентификации", "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
