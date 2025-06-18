package ru.avdonin.client.settings.language;

import ru.avdonin.client.settings.BaseFactory;
import ru.avdonin.template.exceptions.FactoryException;

import java.util.Locale;

public class FactoryLanguage extends BaseFactory {
    private static FactoryLanguage factory;
    private static FrameLanguage language;
    private static final String LANGUAGE_CONFIG = "language";

    private FactoryLanguage() {
    }

    public static FactoryLanguage getFactory() {
        return factory == null ? factory = new FactoryLanguage() : factory;
    }

    @Override
    public BaseDictionary getSettings() {
        return getFrameSettings().getDictionary();
    }

    @Override
    public FrameLanguage getFrameSettings() {
        if (language == null) {
            if (getProperty(LANGUAGE_CONFIG).equals("SYSTEM")) language = getLanguageSystem();
            else language = FrameLanguage.valueOf(getProperty(LANGUAGE_CONFIG));
        }
        return language;
    }

    public void setLanguage(FrameLanguage newLanguage) {
        try {
            updateProperty(LANGUAGE_CONFIG, newLanguage.name());
            if (newLanguage == FrameLanguage.SYSTEM) language = getLanguageSystem();
            else language = newLanguage;
        } catch (Exception e) {
            throw new FactoryException("Failed to set language", e);
        }
    }

    public void setLanguage(String newLanguage) {
        try {
            FrameLanguage language = null;
            for (FrameLanguage l : FrameLanguage.values())
                if (l.getSelectedSetting().equals(newLanguage)) language = l;

            if (language == null) throw new RuntimeException();
            setLanguage(language);
        } catch (IllegalArgumentException e) {
            throw new FactoryException("Invalid language: " + newLanguage, e);
        }
    }

    private FrameLanguage getLanguageSystem() {
        Locale systemLocale = Locale.getDefault();
        String languageStr = systemLocale.getLanguage().toUpperCase();
        for (FrameLanguage l : FrameLanguage.values())
            if (l.toString().equals(languageStr)) return l;

        return FrameLanguage.EN;
    }
}