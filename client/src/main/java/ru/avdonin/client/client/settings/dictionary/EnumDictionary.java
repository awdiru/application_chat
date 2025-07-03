package ru.avdonin.client.client.settings.dictionary;

import lombok.Getter;
import ru.avdonin.client.client.gui.SettingsFrame;
import ru.avdonin.client.client.settings.EnumSettings;
import ru.avdonin.client.client.settings.dictionary.list.*;

@Getter
public enum EnumDictionary implements EnumSettings {
    SYSTEM(new DictionarySystem()),
    RU(new DictionaryRU()),
    EN(new DictionaryEN()),
    SP(new DictionarySP()),
    IT(new DictionaryIT());

    private final BaseDictionary dictionary;
    private final FactoryDictionary factory = FactoryDictionary.getFactory();

    EnumDictionary(BaseDictionary dictionary) {
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
