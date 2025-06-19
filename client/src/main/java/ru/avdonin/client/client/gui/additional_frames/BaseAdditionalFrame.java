package ru.avdonin.client.client.gui.additional_frames;

import javax.swing.*;
import java.awt.*;

public abstract class BaseAdditionalFrame extends JFrame {
    protected void initFrame(String title, Dimension size) {
        setTitle(title);
        setSize(size);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
