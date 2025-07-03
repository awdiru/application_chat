package ru.avdonin.client.client.settings.dictionary;

import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.context.ContextKeys;
import ru.avdonin.client.client.settings.BaseFactory;
import ru.avdonin.client.repository.configs.DefaultConfigs;
import ru.avdonin.template.exceptions.FactoryException;

import java.util.Locale;

import static ru.avdonin.client.client.context.ContextKeys.DICTIONARY;

public class FactoryDictionary extends BaseFactory<EnumDictionary, BaseDictionary> {
    private static FactoryDictionary factory;
    private static EnumDictionary dictionary;
    private static final String LANGUAGE_CONFIG = DefaultConfigs.LANGUAGE.getConfigName();

    private FactoryDictionary() {
    }

    public static FactoryDictionary getFactory() {
        return factory == null ? factory = new FactoryDictionary() : factory;
    }

    @Override
    public BaseDictionary getSettings() {
        return getFrameSettings().getDictionary();
    }

    @Override
    public EnumDictionary getFrameSettings() {
        if (dictionary == null) {
            if (getProperty(LANGUAGE_CONFIG).equals("SYSTEM")) dictionary = getDictionarySystem();
            else dictionary = EnumDictionary.valueOf(getProperty(LANGUAGE_CONFIG));
        }
        return dictionary;
    }

    @Override
    public void setValue(EnumDictionary value) {
        try {
            updateProperty(LANGUAGE_CONFIG, value.name());
            if (value == EnumDictionary.SYSTEM) dictionary = getDictionarySystem();
            else dictionary = value;
            Context.put(DICTIONARY, dictionary);
        } catch (Exception e) {
            throw new FactoryException("Failed to set language", e);
        }
    }

    @Override
    public void setValue(String value) {
        try {
            EnumDictionary dictionary = null;
            for (EnumDictionary l : EnumDictionary.values())
                if (l.getSelectedSetting().equals(value)) dictionary = l;
            if (dictionary == null) throw new RuntimeException();
            setValue(dictionary);
        } catch (IllegalArgumentException e) {
            throw new FactoryException("Invalid language: " + value, e);
        }
    }

    private EnumDictionary getDictionarySystem() {
        Locale systemLocale = Locale.getDefault();
        String languageStr = systemLocale.getLanguage().toUpperCase();
        for (EnumDictionary l : EnumDictionary.values())
            if (l.toString().equals(languageStr)) return l;

        return EnumDictionary.RU;
    }
}