package ru.avdonin.client.settings.language;

import lombok.Getter;

@Getter
public abstract class BaseLanguage {
    protected final String authorization = "Authorization";
    protected final String login = "Login";
    protected final String signup = "SignUp";
    protected final String username = "Username";
    protected final String password = "Password";

    protected final String chat = "Chat";
    protected final String friends = "Friends";
    protected final String requestFriends = "Friend requests";

    protected final String error = "Error";
    protected final String authorizationError = "Authorization error";
    protected final String errorCode = "Error code";
    protected final String statusCode = "Status code";
}
