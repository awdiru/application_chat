package ru.avdonin.client.client.settings.dictionary;

import lombok.Getter;
import ru.avdonin.client.client.settings.BaseSettings;

import javax.swing.*;

@Getter
public abstract class BaseDictionary extends BaseSettings {
    private final String locale = "RU";
    private final String customization = "Русский";
    private final String yes = "Да";
    private final String no = "Нет";
    //Authorization window
    private final String authorization = "Авторизация";
    private final String login = "Войти";
    private final String signup = "Регистрация";
    private final String username = "Имя пользователя";
    private final String password = "Пароль";
    //Chat window
    private final String chat = "Чат";
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
    //Chat status bar
    private final String typing = " печатает...";
    private final String typings = " печатают...";
    //Messages
    private final String edited = "изменено";
    //Chat context menu
    private final String logoutChat = "Выйти из чата";
    private final String logoutChatQuestion = "Вы уверены, что хотите выйти из чата?";
    private final String renameChatCustom = "Переименовать чат (у себя)";
    private final String renameChatAdmin = "Переименовать чат (для всех)";
    private final String rename = "Переименовать";
    private final String addUser = "Добавить пользователя";
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
    private final String settingsSystem = "Система";
    //Errors messages
    private final String error = "Ошибка";
    private final String errorCode = "Код ошибки";
    private final String statusCode = "Статусный код";
    //Month
    private final String january = "январь";
    private final String february = "февраль";
    private final String march = "март";
    private final String april = "апрель";
    private final String may = "май";
    private final String june = "июнь";
    private final String july = "июль";
    private final String august = "август";
    private final String september = "сентябрь";
    private final String october = "октябрь";
    private final String november = "ноябрь";
    private final String december = "декабрь";
    //Weekday
    private final String monday = "Пн";
    private final String tuesday = "Вт";
    private final String wednesday = "Ср";
    private final String thursday = "Чт";
    private final String friday = "Пт";
    private final String saturday = "Сб";
    private final String sunday = "Вс";
    //Exception
    private final String cannotBeRead = "Файл изображения не может быть прочитан";
    //Default symbols
    private final ImageIcon settings = new ImageIcon("data/settings-icon.png");
    private final ImageIcon reboot = new ImageIcon("data/reboot-icon.png");
    private final ImageIcon upArrow = new ImageIcon("data/up-arrow-icon.png");
    private final ImageIcon rightArrow = new ImageIcon("data/right-arrow-icon.png");
    private final ImageIcon burger = new ImageIcon("data/burger-icon.png");
    private final ImageIcon newChat = new ImageIcon("data/new-chat-icon.png");
    private final ImageIcon exit = new ImageIcon("data/exit-icon.png");
    private final ImageIcon changeAvatar = new ImageIcon("data/change-avatar-icon.png");
    private final ImageIcon participants = new ImageIcon("data/participants-chat-icon.png");
    private final ImageIcon paperClip = new ImageIcon("data/paper-clip-icon.png");
    private final ImageIcon defaultImage = new ImageIcon("data/default-icon.png");
    private final ImageIcon pencil = new ImageIcon("data/pencil-icon.png");
    private final ImageIcon delete = new ImageIcon("data/delete-icon.png");
    private final ImageIcon envelope = new ImageIcon("data/envelope-icon.png");
    private final ImageIcon defaultAvatar = new ImageIcon("data/default-avatar.png");

    //numbers
    private final ImageIcon one = new ImageIcon("data/numbers/1.png");
    private final ImageIcon two = new ImageIcon("data/numbers/2.png");
    private final ImageIcon three = new ImageIcon("data/numbers/3.png");
    private final ImageIcon four = new ImageIcon("data/numbers/4.png");
    private final ImageIcon five = new ImageIcon("data/numbers/5.png");
    private final ImageIcon six = new ImageIcon("data/numbers/6.png");
    private final ImageIcon seven = new ImageIcon("data/numbers/7.png");
    private final ImageIcon eight = new ImageIcon("data/numbers/8.png");
    private final ImageIcon nine = new ImageIcon("data/numbers/9.png");
    private final ImageIcon plusNum = new ImageIcon("data/numbers/plus.png");
}
