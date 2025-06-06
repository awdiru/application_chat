package ru.avdonin.server.dictionary.list;

import lombok.Getter;
import ru.avdonin.server.dictionary.AbstractDictionary;

@Getter
public class DictionarySP extends AbstractDictionary {
    private final String addUserIncorrectChatDataException = "No se puede añadir un usuario a un chat privado";
    private final String deleteChatIncorrectUserDataException = "Solo el administrador puede eliminar el chat";
    private final String renameChatAdminIncorrectChatDataException = "Solo el administrador puede renombrar el chat";
    private final String getChatIncorrectChatDataException = "El chat no existe";
    private final String getSecretKeySpecIllegalArgumentException = "Clave inválida. Debe tener 32 caracteres";
    private final String saveMessageIncorrectChatDataException = "Este chat no existe";
    private final String validateIncorrectUserDataException = "Contraseña inválida";
    private final String saveIncorrectLoginException = "El nombre de usuario no puede estar vacío";
    private final String saveIncorrectPasswordException = "La contraseña no puede estar vacía";
    private final String saveIncorrectUserDataException = "Ya existe un usuario con ese nombre";
    private final String createChatIncorrectChatDataException = "El nombre del chat no puede estar vacío";
    private final String getPrivateChatIncorrectChatDataException = "No hay ningún chat creado por el usuario";

    public String getSaveMessageIncorrectUserDataException(String sender) {
        return "El usuario con nombre " + sender + " no existe";
    }

    public String getRenameChatCustomIncorrectChatDataException(String username, String chatName) {
        return "El usuario " + username + " no es miembro del chat " + chatName;
    }

    public String getGetUserIncorrectUserDataException(String username) {
        return "El usuario " + username + " no existe";
    }

    public String getEncryptRuntimeException(String message) {
        return "Error de cifrado: " + message;
    }

    public String getDecryptRuntimeException(String message) {
        return "Error de descifrado: " + message;
    }

    public String getSearchUserByUsernameIncorrectUserDataException(String username) {
        return "El usuario con nombre " + username + " no existe";
    }
}
