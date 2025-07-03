package ru.avdonin.client.client.settings;

import lombok.Getter;
import ru.avdonin.client.client.gui.SettingsFrame;
import ru.avdonin.client.client.settings.dictionary.FactoryDictionary;
import ru.avdonin.client.client.settings.time_zone.FactoryTimeZone;

@Getter
public enum Settings {
    LANGUAGE(FactoryDictionary.getFactory().getSettings().getSettingsLanguage(), FactoryDictionary.getFactory()),
    TIME_ZONE(FactoryDictionary.getFactory().getSettings().getSettingsTimeZone(), FactoryTimeZone.getFactory()),;

    private final String settingsName;
    private final BaseFactory<?, ?> factory;

    Settings(String settingsName, BaseFactory<?, ?> factory) {
        this.settingsName = settingsName;
        this.factory = factory;
    }

    public static Settings getSettings(String settingsName) {
        for (Settings s : Settings.values()) if (settingsName.equals(s.settingsName)) return s;
        return null;
    }

    public static void getFrameSettings() {
        SettingsFrame.getSettingsFrame();
    }
}
