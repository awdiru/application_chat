package ru.avdonin.client.client.gui.additional.frames;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client.context.ContextKeysEnum.*;

public abstract class BaseAdditionalFrame extends JFrame {
    protected void initFrame(String title, Dimension size) {
        setTitle(title);
        setSize(size);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected BaseDictionary getDictionary() {
        return Context.get(DICTIONARY);
    }

    protected MainFrame getMainFrame() {
        return Context.get(MAIN_FRAME);
    }

    protected Client getClient() {
        return Context.get(CLIENT);
    }
}
