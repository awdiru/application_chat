package ru.avdonin.server.service;

import ru.avdonin.server.dictionary.BaseDictionary;
import ru.avdonin.server.dictionary.list.DictionaryEN;
import ru.avdonin.server.dictionary.list.DictionaryIT;
import ru.avdonin.server.dictionary.list.DictionaryRU;
import ru.avdonin.server.dictionary.list.DictionarySP;

public abstract class AbstractService {
    private static final BaseDictionary RU = new DictionaryRU();
    private static final BaseDictionary EN = new DictionaryEN();
    private static final BaseDictionary SP = new DictionarySP();
    private static final BaseDictionary IT = new DictionaryIT();

    protected BaseDictionary getDictionary(String locale) {
        return switch (locale) {
            case "RU" -> RU;
            case "SP" -> SP;
            case "IT" -> IT;
            default -> EN;
        };
    }
}
