package ru.avdonin.client.client.constatnts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public enum Constants {
    MOUSE_ENTERED_ITEM_COLOR(new Color(183, 250, 211), Color.class),
    BACKGROUND_COLOR(UIManager.getColor("Panel.background"), Color.class),

    SEND_MESSAGE(KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER,
            InputEvent.CTRL_DOWN_MASK
    ), KeyStroke.class),
    SEND_MESSAGE_KEY("sendAction", String.class);



    private final Object value;
    private final Class<?> aClass;

    Constants(Object value, Class<?> aClass) {
        this.value = value;
        this.aClass = aClass;
    }

    public<V> V getValue() {
        return (V) aClass.cast(value);
    }
}
