package ru.avdonin.client.settings;

import lombok.Getter;
import ru.avdonin.client.settings.language.FactoryLanguage;

import javax.swing.*;
import java.awt.*;

@Getter
public enum Settings {
    LANGUAGE(FactoryLanguage.getFactory().getSettings().getSettingsLanguage(), FactoryLanguage.getFactory());

    private final String settingsName;
    private final BaseFactory factory;

    Settings(String settingsName, BaseFactory factory) {
        this.settingsName = settingsName;
        this.factory = factory;
    }

    public static Settings getSettings(String settingsName) {
        for (Settings s : Settings.values()) if (settingsName.equals(s.settingsName)) return s;
        return null;
    }

    public static void getFrame() {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingsTitle());
        main.setSize(200, 300);
        main.setLocationRelativeTo(null);

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (Settings s : Settings.values())
            settingsModel.addElement(s.getSettingsName() + " -> " + s.factory.getFrameSettings().getSelectedSetting());
        settingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    String settingsName = selected.split(" ")[0];
                    Settings s = Settings.getSettings(settingsName);
                    if (s != null) {
                        main.dispose();
                        s.getFactory().getFrameSettings().getFrame();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(settingsList);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        main.add(scrollPane);
        main.setVisible(true);
    }
}
