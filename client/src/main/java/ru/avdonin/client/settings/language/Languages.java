package ru.avdonin.client.settings.language;

import lombok.Getter;
import ru.avdonin.client.settings.language.list.LanguageEN;
import ru.avdonin.client.settings.language.list.LanguageRU;
import ru.avdonin.client.settings.language.list.LanguageSP;

@Getter
public enum Languages {
    RU(new LanguageRU(), "Русский"),
    EN(new LanguageEN(), "English"),
    SP(new LanguageSP(), "Español");

    private final BaseLanguage language;
    private final String languageName;

    Languages(BaseLanguage language, String languageName) {
        this.language = language;
        this.languageName = languageName;
    }
}
