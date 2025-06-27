package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseDictionary;

@Getter
public class DictionaryRU extends BaseDictionary {
    private final String locale = "RU";
    private final String customization = "Русский";
    private final String yes = "Да";
    private final String no = "Нет";
    private final String confirmation = "Подтверждение";
    //Authorization window
    private final String authorization = "Авторизация";
    private final String login = "Войти";
    private final String signup = "Регистрация";
    private final String username = "Имя пользователя";
    private final String password = "Пароль";
    //Chat window
    private final String chat = "Чат";
    private final String chats = "Чаты";
    private final String invitations = "Приглашения";
    //Chats window
    private final String addChatTitle = "Создать чат";
    private final String chatName = "Название чата";
    private final String friendsName = "Имя друга";
    private final String privateChat = "Приватный чат";
    private final String publicChat = "Публичный чат";
    private final String createChat = "Создать чат";
    private final String attachImage = "Прикрепить изображение";
    private final String images = "Изображения";
    //Chat context menu
    private final String logoutChat = "Покинуть чат";
    private final String logoutChatQuestion = "Вы уверены, что хотите выйти из чата?";
    private final String renameChatCustom = "Переименовать чат (для себя)";
    private final String renameChatAdmin = "Переименовать чат (для всех)";
    private final String rename = "Переименовать";
    private final String addUser = "Добавить пользователя";
    private final String addUserQuestion = "Имя пользователя";
    private final String add = "Добавить";
    private final String deleteChat = "Удалить чат";
    //invite context menu
    private final String confirmInvite = "Принять приглашение";
    private final String rejectInvite = "Отклонить приглашение";
    //Message context menu
    private final String changeMessage = "Изменить сообщение";
    private final String deleteMessage = "Удалить сообщение";
    //Settings window
    private final String settingsTitle = "Настройки";
    private final String settingsLanguage = "Язык";
    private final String settingsTimeZone = "Часовой пояс";
    private final String settingsSystem = "Системные";
    private final String restartProgram = "Для применения изменений необходимо перезапустить программу.";
    private final String warning = "Предупреждение";
    //Errors messages
    private final String error = "Ошибка";
    private final String authorizationError = "Ошибка авторизации";
    private final String errorCode = "Код ошибки";
    private final String statusCode = "Статусный код";
    //Month (Родительный падеж)
    private final String january = "января";
    private final String february = "февраля";
    private final String march = "марта";
    private final String april = "апреля";
    private final String may = "мая";
    private final String june = "июня";
    private final String july = "июля";
    private final String august = "августа";
    private final String september = "сентября";
    private final String october = "октября";
    private final String november = "ноября";
    private final String december = "декабря";
    //Weekday
    private final String monday = "Пн";
    private final String tuesday = "Вт";
    private final String wednesday = "Ср";
    private final String thursday = "Чт";
    private final String friday = "Пт";
    private final String saturday = "Сб";
    private final String sunday = "Вс";
    //Exception
    private final String cannotBeRead = "Невозможно прочитать файл изображения";
    private final String emptyMessage = "Сообщение не может быть пустым";
}
