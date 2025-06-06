package ru.avdonin.server.service;

import ru.avdonin.server.dictionary.AbstractDictionary;
import ru.avdonin.server.dictionary.list.DictionaryEN;
import ru.avdonin.server.dictionary.list.DictionaryIT;
import ru.avdonin.server.dictionary.list.DictionaryRU;
import ru.avdonin.server.dictionary.list.DictionarySP;

public abstract class AbstractService {
    private static final AbstractDictionary RU = new DictionaryRU();
    private static final AbstractDictionary EN = new DictionaryEN();
    private static final AbstractDictionary SP = new DictionarySP();
    private static final AbstractDictionary IT = new DictionaryIT();

    protected AbstractDictionary getDictionary(String locale) {
        return switch (locale) {
            case "RU" -> RU;
            case "SP" -> SP;
            case "IT" -> IT;
            default -> EN;
        };
    }
}
