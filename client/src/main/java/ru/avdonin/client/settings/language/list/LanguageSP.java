package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseLanguage;

@Getter
public class LanguageSP extends BaseLanguage {
    private final String authorization = "Autorización";
    private final String login = "Iniciar sesión";
    private final String signup = "Registrarse";
    private final String username = "Nombre de usuario";
    private final String password = "Contraseña";

    private final String chat = "Chat";
    private final String friends = "Amigos";
    private final String requestFriends = "Solicitudes de amistad";

    private final String error = "Error";
    private final String authorizationError = "Error de autorización";
    private final String errorCode = "Código de error";
    private final String statusCode = "Estado del error";
}
