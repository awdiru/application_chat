package ru.avdonin.client.client.gui.my_code;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.settings.language.LanguageProcessor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame {
    private final Client client;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(Client client) {
        this.client = client;
        initUi();
    }

    private void initUi() {
        setTitle(LanguageProcessor.authorization());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton(LanguageProcessor.login());
        JButton signupButton = new JButton(LanguageProcessor.signup());

        panel.add(new JLabel(LanguageProcessor.username()));
        panel.add(usernameField);
        panel.add(new JLabel(LanguageProcessor.password()));
        panel.add(passwordField);

        panel.add(loginButton);
        panel.add(signupButton);

        loginButton.addActionListener(e -> tryLogin("/login"));
        signupButton.addActionListener(e -> tryLogin("/signup"));

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
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            LanguageProcessor.authorizationError(), LanguageProcessor.error(),
                            JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    dispose();
                    client.connect(username);
                    new MainFrame(client, username).setVisible(true);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            LanguageProcessor.authorizationError(), LanguageProcessor.error(),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
