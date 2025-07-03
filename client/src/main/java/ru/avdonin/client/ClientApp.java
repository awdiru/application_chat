package ru.avdonin.client;

import ru.avdonin.client.client.gui.LoginFrame;

import javax.swing.*;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
