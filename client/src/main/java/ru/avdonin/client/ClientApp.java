package ru.avdonin.client;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.my_code.LoginFrame;

import javax.swing.*;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            new LoginFrame(client).setVisible(true);
        });
    }
}
