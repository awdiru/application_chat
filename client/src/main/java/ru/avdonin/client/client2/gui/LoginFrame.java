package ru.avdonin.client.client2.gui;

import ru.avdonin.client.client2.Client;
import ru.avdonin.client.client2.context.Context;
import ru.avdonin.client.client2.service.BaseFrame;
import ru.avdonin.client.client2.service.dictionary.BaseDictionary;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client2.context.ContextKeysEnum.DICTIONARY;
import static ru.avdonin.client.client2.context.ContextKeysEnum.USERNAME;

public class LoginFrame extends BaseFrame {
    private final Client client = getClient();
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        initUi();
        setVisible(true);
    }

    private void initUi() {
        BaseDictionary dictionary = getDictionary();
        setTitle(dictionary.getAuthorization());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton(dictionary.getLogin());
        JButton signupButton = new JButton(dictionary.getSignup());

        panel.add(new JLabel(dictionary.getUsername()));
        panel.add(usernameField);
        panel.add(new JLabel(dictionary.getPassword()));
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
                    FrameHelper.errorHandler(e, LoginFrame.this);
                    dispose();
                    new LoginFrame();
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    if (!get()) return;
                    dispose();
                    Context.put(USERNAME, username);
                    client.connect();
                    new MainFrame();
                } catch (Exception e) {
                    FrameHelper.errorHandler(e, LoginFrame.this);
                }
            }
        }.execute();
    }
}
