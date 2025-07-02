package ru.avdonin.client.client.constatnts;

import lombok.Getter;
import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.settings.language.BaseDictionary;
import ru.avdonin.client.repository.ConfigsRepository;

import java.util.Map;

@Getter
public enum KeysCtx {
    DICTIONARY("dictionary", BaseDictionary.class),
    CLIENT("client", Client.class),
    USERNAME("username", String.class),
    AVATARS("avatars", Map.class),
    MAIN_FRAME("mainFrame", MainFrame.class),
    CONFIG_REP("config_rep", ConfigsRepository.class);

    private final String key;
    private final Class<?> valueClass;

    KeysCtx(String key, Class<?> valueClass) {
        this.key = key;
        this.valueClass = valueClass;
    }
}
