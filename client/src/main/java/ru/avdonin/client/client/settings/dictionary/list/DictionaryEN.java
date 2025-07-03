package ru.avdonin.client.client.settings.dictionary.list;

import lombok.Getter;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;

@Getter
public class DictionaryEN extends BaseDictionary {
    private final String locale = "EN";
    private final String customization = "English";
    private final String yes = "Yes";
    private final String no = "No";
    //Authorization window
    private final String authorization = "Authorization";
    private final String login = "Login";
    private final String signup = "SignUp";
    private final String username = "Username";
    private final String password = "Password";
    //Chat window
    private final String chat = "Chat";
    private final String invitations = "Invitations";
    //Chats window
    private final String addChatTitle = "Add chat";
    private final String chatName = "Chat name";
    private final String friendsName = "Friend's name";
    private final String privateChat = "Private chat";
    private final String publicChat = "Public chat";
    private final String createChat = "Create chat";
    private final String attachImage = "Attach image";
    private final String images = "Images";
    //Chat status bar
    private final String typing = " typing...";
    private final String typings = " typing...";
    //Messages
    private final String edited = "edited";
    //Chat context menu
    private final String logoutChat = "Log out of the chat";
    private final String logoutChatQuestion = "Are you sure you want to exit the chat?";
    private final String renameChatCustom = "Rename the chat (at home)";
    private final String renameChatAdmin = "Rename the chat (for everyone)";
    private final String rename = "Rename it";
    private final String addUser = "Add a user to chats";
    private final String add = "Add";
    private final String deleteChat = "Delete chat";
    //invite context menu
    private final String confirmInvite = "Accept the invitation";
    private final String rejectInvite = "Decline the invitation";
    //Message context menu
    private final String changeMessage = "Change the message";
    private final String deleteMessage = "Delete a message";
    //Settings window
    private final String settingsTitle = "Settings";
    private final String settingsLanguage = "Language";
    private final String settingsTimeZone = "Time zone";
    private final String settingsSystem = "System";
    //Errors messages
    private final String error = "Error";
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
    //Exception
    private final String cannotBeRead = "The image file cannot be read";}
