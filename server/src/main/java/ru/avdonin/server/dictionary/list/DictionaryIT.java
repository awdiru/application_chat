package ru.avdonin.server.dictionary.list;

import lombok.Getter;
import ru.avdonin.server.dictionary.BaseDictionary;

@Getter
public class DictionaryIT extends BaseDictionary {
    private final String addUserIncorrectChatDataException = "Non è possibile aggiungere un utente a una chat privata";
    private final String deleteChatIncorrectUserDataException = "Solo l'amministratore può eliminare la chat";
    private final String renameChatAdminIncorrectChatDataException = "Solo l'amministratore può rinominare la chat";
    private final String getChatIncorrectChatDataException = "La chat non esiste";
    private final String getSecretKeySpecIllegalArgumentException = "Chiave non valida. Deve essere di 32 caratteri";
    private final String saveMessageIncorrectChatDataException = "Questa chat non esiste";
    private final String validateIncorrectUserDataException = "Password non valida";
    private final String saveIncorrectLoginException = "Il nome utente non può essere vuoto";
    private final String saveIncorrectPasswordException = "La password non può essere vuota";
    private final String saveIncorrectUserDataException = "Un utente con questo nome è già registrato";
    private final String createChatIncorrectChatDataException = "Il nome della chat non può essere vuoto";

    public String getSaveMessageIncorrectUserDataException(String sender) {
        return "L'utente con nome " + sender + " non esiste";
    }

    public String getRenameChatCustomIncorrectChatDataException(String username, String chatName) {
        return "L'utente " + username + " non è membro della chat " + chatName;
    }

    public String getGetUserIncorrectUserDataException(String username) {
        return "L'utente " + username + " non esiste";
    }

    public String getEncryptRuntimeException(String message) {
        return "Errore di crittografia: " + message;
    }

    public String getDecryptRuntimeException(String message) {
        return "Errore di decrittazione: " + message;
    }

    public String getSearchUserByUsernameIncorrectUserDataException(String username) {
        return "L'utente con nome " + username + " non esiste";
    }
}
