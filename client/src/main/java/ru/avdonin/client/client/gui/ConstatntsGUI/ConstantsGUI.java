package ru.avdonin.client.client.gui.ConstatntsGUI;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@Getter
public enum ConstantsGUI {
    BACKGROUND_COLOR(UIManager.getColor("Panel.background")),
    SEND_MESSAGE(KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER,
            InputEvent.CTRL_DOWN_MASK
    )),
    SEND_MESSAGE_KEY("sendAction");

    private final Object value;

    ConstantsGUI(Object value) {
        this.value = value;
    }
}
