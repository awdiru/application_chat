package ru.avdonin.client.client.gui.additional.panels;

import ru.avdonin.client.client.Context;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.language.BaseDictionary;

import javax.swing.*;

import static ru.avdonin.client.client.constatnts.KeysCtx.*;

public class BaseJPanel extends JPanel {
    protected final BaseDictionary dictionary = Context.get(DICTIONARY);
    protected final MainFrame mainFrame = Context.get(MAIN_FRAME);
}
