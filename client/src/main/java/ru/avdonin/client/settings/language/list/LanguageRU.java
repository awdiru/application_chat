package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseLanguage;

@Getter
public class LanguageRU extends BaseLanguage {
    private final String authorization = "Авторизация";
    private final String login = "Войти";
    private final String signup = "Зарегистрироваться";
    private final String username = "Имя пользователя";
    private final String password = "Пароль";

    private final String chat = "Чат";
    private final String friends = "Друзья";
    private final String requestFriends = "Запросы в друзья";

    private final String error = "Ошибка";
    private final String authorizationError = "Ошибка авторизации";
    private final String errorCode = "Код ошибки";
    private final String statusCode = "Статус ошибки";
}
