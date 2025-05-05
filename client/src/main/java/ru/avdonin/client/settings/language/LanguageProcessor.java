package ru.avdonin.client.settings.language;

public class LanguageProcessor {
    private static BaseLanguage language = Languages.RU.getLanguage();

    private LanguageProcessor() {
    }

    public static String authorization() {
        return language.getAuthorization();
    }

    public static String login() {
        return language.getLogin();
    }

    public static String signup() {
        return language.getSignup();
    }

    public static String username() {
        return language.getUsername();
    }

    public static String password() {
        return language.getPassword();
    }

    public static String chat() {
        return language.getChat();
    }

    public static String friends() {
        return language.getFriends();
    }

    public static String requestFriends() {
        return language.getRequestFriends();
    }

    public static String error() {
        return language.getError();
    }

    public static String authorizationError() {
        return language.getAuthorizationError();
    }

    public static String errorCode() {
        return language.getErrorCode();
    }

    public static String statusCode() {
        return language.getStatusCode();
    }

    public static void setLanguage(Languages language) {
        LanguageProcessor.language = language.getLanguage();
    }
}
