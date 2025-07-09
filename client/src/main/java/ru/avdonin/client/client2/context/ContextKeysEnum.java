package ru.avdonin.client.client2.context;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.client.client.settings.time_zone.BaseTimeZone;
import ru.avdonin.client.repository.ConfigsRepository;

public enum ContextKeysEnum {
    DICTIONARY("dictionary", BaseDictionary.class),
    TIME_ZONE("timeZone", BaseTimeZone.class),
    CLIENT("client", Client.class),
    USERNAME("username", String.class),
    MAIN_FRAME("mainFrame", MainFrame.class),
    CONFIG_REP("config_rep", ConfigsRepository.class);

    @Getter
    private final String key;
    private final Class<?> aClass;

    ContextKeysEnum(String key, Class<?> aClass) {
        this.key = key;
        this.aClass = aClass;
    }

    Class<?> getAClass() {
        return aClass;
    }
}
