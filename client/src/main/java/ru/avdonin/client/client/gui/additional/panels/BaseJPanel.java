package ru.avdonin.client.client.gui.additional.panels;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;

import javax.swing.*;

import static ru.avdonin.client.client.context.ContextKeys.*;

public class BaseJPanel extends JPanel {
    protected BaseDictionary getDictionary() {
        return Context.get(DICTIONARY);
    }

    protected MainFrame getMainFrame() {
        return Context.get(MAIN_FRAME);
    }

    protected Client getClient() {
        return Context.get(CLIENT);
    }

    protected String getUsername() {
        return Context.get(USERNAME);
    }
}
