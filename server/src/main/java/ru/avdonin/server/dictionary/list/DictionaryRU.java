package ru.avdonin.server.dictionary.list;

import lombok.Getter;
import ru.avdonin.server.dictionary.AbstractDictionary;

@Getter
public class DictionaryRU extends AbstractDictionary {
    private final String addUserIncorrectChatDataException = "Невозможно добавить пользователя в приватный чат";
    private final String deleteChatIncorrectUserDataException = "Только администратор может удалить чат";
    private final String renameChatAdminIncorrectChatDataException = "Только администратор может переименовать чат";
    private final String getChatIncorrectChatDataException = "Чат не существует";
    private final String getSecretKeySpecIllegalArgumentException = "Недопустимый ключ. Должен быть 32 символа";
    private final String saveMessageIncorrectChatDataException = "Данный чат не существует";
    private final String validateIncorrectUserDataException = "Неверный пароль";
    private final String saveIncorrectLoginException = "Имя пользователя не может быть пустым";
    private final String saveIncorrectPasswordException = "Пароль не может быть пустым";
    private final String saveIncorrectUserDataException = "Пользователь с таким именем уже зарегистрирован";
    private final String createChatIncorrectChatDataException = "Название чата не может быть пустым";
    private final String getPrivateChatIncorrectChatDataException = "Нет чата, созданного пользователем";

    public String getSaveMessageIncorrectUserDataException(String sender) {
        return "Пользователь с именем " + sender + " не существует";
    }

    public String getRenameChatCustomIncorrectChatDataException(String username, String chatName) {
        return "Пользователь " + username + " не является участником чата " + chatName;
    }

    public String getGetUserIncorrectUserDataException(String username) {
        return "Пользователь " + username + " не существует";
    }

    public String getEncryptRuntimeException(String message) {
        return "Ошибка шифрования: " + message;
    }

    public String getDecryptRuntimeException(String message) {
        return "Ошибка дешифрования: " + message;
    }

    public String getSearchUserByUsernameIncorrectUserDataException(String username) {
        return "Пользователь с именем " + username + " не существует";
    }
}
