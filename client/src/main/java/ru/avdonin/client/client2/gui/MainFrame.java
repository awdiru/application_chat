package ru.avdonin.client.client2.gui;

import ru.avdonin.client.client2.service.BaseFrame;

public class MainFrame extends BaseFrame {
    public MainFrame() {
    }

    private void initUi() {
        setTitle(getDictionary().getChat() + " - " + getUsername());
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add();
        setVisible(true);
    }
}
