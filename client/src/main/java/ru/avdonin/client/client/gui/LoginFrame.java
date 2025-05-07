package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final Client client;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private BaseDictionary language;

    public LoginFrame(Client client) {
        this.client = client;
        language = FactoryLanguage.getFactory().getSettings();
        initUi();
    }

    private void initUi() {
        setTitle(language.getAuthorization());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton(language.getLogin());
        JButton signupButton = new JButton(language.getSignup());

        panel.add(new JLabel(language.getUsername()));
        panel.add(usernameField);
        panel.add(new JLabel(language.getPassword()));
        panel.add(passwordField);

        panel.add(loginButton);
        panel.add(signupButton);

        loginButton.addActionListener(e -> tryLogin("/login"));
        signupButton.addActionListener(e -> tryLogin("/signup"));
        passwordField.addActionListener(e -> tryLogin("/login"));

        add(panel);
    }

    private void tryLogin(String path) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    client.login(username, password, path);
                    return true;
                } catch (Exception e) {
                    errorHandler(e);
                    dispose();
                    new LoginFrame(client).setVisible(true);
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    if (!get()) return;
                    dispose();
                    client.connect(username);
                    new MainFrame(client, username).setVisible(true);
                } catch (Exception e) {
                    errorHandler(e);
                }
            }
        }.execute();
    }

    private void errorHandler(Exception e) {
        if (e.getMessage() == null || e.getMessage().isEmpty()) return;
        JOptionPane.showMessageDialog(LoginFrame.this,
                e.getMessage(), language.getError(),
                JOptionPane.ERROR_MESSAGE);
    }
}
