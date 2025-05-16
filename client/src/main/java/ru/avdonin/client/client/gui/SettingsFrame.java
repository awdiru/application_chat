package ru.avdonin.client.client.gui;

import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.client.settings.language.FrameLanguage;
import ru.avdonin.client.settings.time_zone.FactoryTimeZone;
import ru.avdonin.client.settings.time_zone.FrameTimeZone;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame {
    private static final Integer width = 250;
    private static final Integer height = 300;
    private static final Dimension screenSize = new Dimension(width, height);

    public static void getFrame() {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingsTitle());
        main.setSize(width, height);
        main.setLocationRelativeTo(null);

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
        scrollPane.setPreferredSize(screenSize);
        main.add(scrollPane);
        main.setVisible(true);
    }

    public static void getTimeSettingsFrame(FactoryTimeZone factory) {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingsTimeZone());
        main.setSize(width, height);
        main.setLocationRelativeTo(null);

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (FrameTimeZone l : FrameTimeZone.values())
            settingsModel.addElement(l.getSelectedSetting());

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    factory.setTimeZone(selected);
                    main.dispose();
                    JOptionPane.showMessageDialog(main,
                            FactoryLanguage.getFactory().getFrameSettings().getLanguage().getRestartProgram(), FactoryLanguage.getFactory().getFrameSettings().getLanguage().getWarning(),
                            JOptionPane.WARNING_MESSAGE);
                    Settings.getFrameSettings();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(settingsList);
        scrollPane.setPreferredSize(screenSize);
        main.add(scrollPane);
        main.setVisible(true);
    }

    public static void getLanguageSettingsFrame(FactoryLanguage factory) {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingsLanguage());
        main.setSize(width, height);
        main.setLocationRelativeTo(null);

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (FrameLanguage l : FrameLanguage.values())
            settingsModel.addElement(l.getSelectedSetting());

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    factory.setLanguage(selected);
                    main.dispose();
                    BaseDictionary language = FactoryLanguage.getFactory().getFrameSettings().getLanguage();
                    JOptionPane.showMessageDialog(main,
                            language.getRestartProgram(), language.getWarning(),
                            JOptionPane.WARNING_MESSAGE);
                    Settings.getFrameSettings();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(settingsList);
        scrollPane.setPreferredSize(screenSize);
        main.add(scrollPane);
        main.setVisible(true);
    }
}
