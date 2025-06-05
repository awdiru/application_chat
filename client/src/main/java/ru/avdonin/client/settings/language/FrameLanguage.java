package ru.avdonin.client.settings.language;

import lombok.Getter;
import ru.avdonin.client.client.gui.SettingsFrame;
import ru.avdonin.client.settings.FrameSettings;
import ru.avdonin.client.settings.language.list.*;

@Getter
public enum FrameLanguage implements FrameSettings {
    SYSTEM(new DictionarySystem()),
    RU(new DictionaryRU()),
    EN(new DictionaryEN()),
    SP(new DictionarySP()),
    IT(new DictionaryIT());

    private final BaseDictionary dictionary;
    private final FactoryLanguage factory = FactoryLanguage.getFactory();

    FrameLanguage(BaseDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void getFrame() {
        SettingsFrame.getLanguageSettingsFrame(factory);
    }

    @Override
    public String getSelectedSetting() {
        return dictionary.getCustomization();
    }
}
