package ru.avdonin.client.client.gui;

import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.settings.BaseFactory;
import ru.avdonin.client.client.settings.EnumSettings;
import ru.avdonin.client.client.settings.Settings;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.client.client.settings.dictionary.FactoryDictionary;
import ru.avdonin.client.client.settings.dictionary.EnumDictionary;
import ru.avdonin.client.client.settings.time_zone.FactoryTimeZone;
import ru.avdonin.client.client.settings.time_zone.EnumTimeZone;

import javax.swing.*;
import java.awt.*;

import static ru.avdonin.client.client.context.ContextKeysEnum.*;

public class SettingsFrame {
    private static final Integer WIDTH = 250;
    private static final Integer HEIGHT = 300;
    private static final Dimension SCREEN_SIZE = new Dimension(WIDTH, HEIGHT);

    public static void getSettingsFrame() {
        BaseDictionary dictionary = getDictionary();
        JFrame main = initFrame(dictionary.getSettingsTitle());

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (Settings s : Settings.values())
            settingsModel.addElement(s.getSettingsName() + " > " + s.getFactory().getFrameSettings().getSelectedSetting());

        settingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    String settingsName = selected.split(" > ")[0];
                    Settings s = Settings.getSettings(settingsName);
                    if (s != null) {
                        main.dispose();
                        s.getFactory().getFrameSettings().getFrame();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(settingsList);
        scrollPane.setPreferredSize(SCREEN_SIZE);
        main.add(scrollPane);
    }

    public static void getTimeSettingsFrame(FactoryTimeZone factory) {
        BaseDictionary dictionary = getDictionary();
        JFrame main = initFrame(dictionary.getSettingsTimeZone());
        JScrollPane scrollPane = initScrollPane(EnumTimeZone.class, factory, main);
        main.add(scrollPane);
    }

    public static void getLanguageSettingsFrame(FactoryDictionary factory) {
        BaseDictionary dictionary = getDictionary();
        JFrame main = initFrame(dictionary.getSettingsLanguage());
        JScrollPane scrollPane = initScrollPane(EnumDictionary.class, factory, main);
        main.add(scrollPane);
    }

    private static JFrame initFrame(String title) {
        JFrame frame = new JFrame();
        frame.setTitle(title);
        frame.setSize(SCREEN_SIZE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    private static <E extends Enum<E> & EnumSettings> JScrollPane initScrollPane(
            Class<E> enumClass,
            BaseFactory<?, ?> factory,
            JFrame main) {

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (E e : enumClass.getEnumConstants())
            settingsModel.addElement(e.getSelectedSetting());

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    factory.setValue(selected);
                    main.dispose();
                    Settings.getFrameSettings();
                }
            }
        });
        return new JScrollPane(settingsList);
    }

    private static BaseDictionary getDictionary() {
        return Context.get(DICTIONARY);
    }
}
