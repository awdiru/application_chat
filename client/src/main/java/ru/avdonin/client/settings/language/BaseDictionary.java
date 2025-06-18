package ru.avdonin.client.settings.language;

import lombok.Getter;
import ru.avdonin.client.settings.BaseSettings;

import javax.swing.*;

@Getter
public abstract class BaseDictionary extends BaseSettings {
    private final String locale = "EN";
    private final String customization = "English";
    private final String yes = "Yes";
    private final String no = "No";
    private final String confirmation = "Confirmation";
    //Authorization window
    private final String authorization = "Authorization";
    private final String login = "Login";
    private final String signup = "SignUp";
    private final String username = "Username";
    private final String password = "Password";
    //Chat window
    private final String chat = "Chat";
    private final String chats = "Chats";
    private final String invitations = "Invitations";
    //Status bar
    private final String changeUser = "Change User";
    //Chat status bar
    private final String participants = "Participants";
    //Chats window
    private final String addChatTitle = "Add chat";
    private final String chatName = "Chat name";
    private final String friendsName = "Friend's name";
    private final String privateChat = "Private chat";
    private final String publicChat = "Public chat";
    private final String createChat = "Create chat";
    //Chat context menu
    private final String logoutChat = "Log out of the chat";
    private final String logoutChatQuestion = "Are you sure you want to exit the chat?";
    private final String renameChatCustom = "Rename the chat (at home)";
    private final String renameChatAdmin = "Rename the chat (for everyone)";
    private final String rename = "Rename it";
    private final String addUser = "Add a user to chats";
    private final String addUserQuestion = "Username";
    private final String add = "Add";
    private final String deleteChat = "Delete chat";
    //invite context menu
    private final String confirmInvite = "Accept the invitation";
    private final String rejectInvite = "Decline the invitation";
    //Settings window
    private final String settingsTitle = "Settings";
    private final String settingsLanguage = "Language";
    private final String settingsTimeZone = "Time zone";
    private final String settingsSystem = "System";
    private final String restartProgram = "In order for the changes to apply, it is necessary to restart the program.";
    private final String warning = "Warning";
    //Errors messages
    private final String error = "Error";
    private final String authorizationError = "Authorization error";
    private final String errorCode = "Error code";
    private final String statusCode = "Status code";
    //Month
    private final String january = "january";
    private final String february = "february";
    private final String march = "march";
    private final String april = "april";
    private final String may = "may";
    private final String june = "june";
    private final String july = "july";
    private final String august = "august";
    private final String september = "september";
    private final String october = "october";
    private final String november = "november";
    private final String december = "december";
    //Weekday
    private final String monday = "Mon";
    private final String tuesday = "Tue";
    private final String wednesday = "Wed";
    private final String thursday = "Thu";
    private final String friday = "Fri";
    private final String saturday = "Sat";
    private final String sunday = "Sun";
    //Default symbols
    private final ImageIcon plus = new ImageIcon("data/plus-icon.png");
    private final ImageIcon minus = new ImageIcon("date/minus-icon.png");
    private final ImageIcon settings = new ImageIcon("data/settings-icon.png");
    private final ImageIcon ellipsis = new ImageIcon("data/ellipsis-icon.png");
    private final ImageIcon reboot = new ImageIcon("data/reboot-icon.png");
    private final ImageIcon upArrow = new ImageIcon("data/up-arrow-icon.png");
}
