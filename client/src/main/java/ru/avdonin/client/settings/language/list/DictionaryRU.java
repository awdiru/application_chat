package ru.avdonin.client.settings.language.list;

import lombok.Getter;
import ru.avdonin.client.settings.language.BaseDictionary;

@Getter
public class DictionaryRU extends BaseDictionary {
    private final String customization = "Русский";
    private final String yes = "Да";
    private final String no = "Нет";
    private final String confirmation = "Подтверждение";
    //Окно авторизации
    private final String authorization = "Авторизация";
    private final String login = "Войти";
    private final String signup = "Зарегистрироваться";
    private final String username = "Имя пользователя";
    private final String password = "Пароль";
    //Окно чата
    private final String chat = "Чат";
    private final String friends = "Друзья";
    private final String requestFriends = "Запросы в друзья";
    //Статус бар
    private final String changeUser = "Сменить пользователя";
    private final String restart = "Перезагрузить";
    //Окно друзей
    private final String addFriendTitle = "Добавить друга";
    private final String rmFriendTitle = "Удалить друга";
    private final String friendName = "Имя друга";
    //Окно принятия запроса в друзья
    private final String confirmFriendTitle = "Подтверждение запроса в друзья";
    private final String confirmFriend = "Принять запрос";
    private final String rejectedFriend = "Отклонить запрос";
    private final String deleteFriend = "Удалить друга";
    //Окно настроек
    private final String settingsTitle = "Настройки";
    private final String settingsLanguage = "Язык";
    private final String settingsTimeZone = "Часовой пояс";
    private final String settingsSystem = "Система";
    private final String restartProgram = "Для того, чтобы изменения применились, необходим перезапуск программы";
    private final String warning = "Предупреждение";
    //Сообщения об ошибках
    private final String error = "Ошибка";
    private final String authorizationError = "Ошибка авторизации";
    private final String errorCode = "Код ошибки";
    private final String statusCode = "Статус ошибки";
    //Месяца
    private final String january = "Января";
    private final String february = "Февраля";
    private final String march = "Марта";
    private final String april = "Апреля";
    private final String may = "Мая";
    private final String june = "Июня";
    private final String july = "Июля";
    private final String august = "Августа";
    private final String september = "Сентября";
    private final String october = "Октября";
    private final String november = "Ноября";
    private final String december = "Декабря";
    //Дни недели
    private final String monday = "Пн";
    private final String tuesday = "Вт";
    private final String wednesday = "Ср";
    private final String thursday = "Чт";
    private final String friday = "Пт";
    private final String saturday = "Сб";
    private final String sunday = "Вс";
}
