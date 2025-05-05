package ru.avdonin.client.client.gui.deepseek;

import ru.avdonin.client.client.Client;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final Client client;
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

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    client.login(username, password, path);
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    dispose();
                    client.connect(username);
                    new MainFrame(client, username).setVisible(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
