package ru.avdonin.client.client.gui.additional.frames;

import ru.avdonin.client.client.Context;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.language.BaseDictionary;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client.constatnts.KeysCtx.*;

public abstract class BaseAdditionalFrame extends JFrame {
    protected final MainFrame parent = Context.get(MAIN_FRAME);
    protected final BaseDictionary dictionary = Context.get(DICTIONARY);

    protected void initFrame(String title, Dimension size) {
        setTitle(title);
        setSize(size);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
