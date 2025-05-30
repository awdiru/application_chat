package ru.avdonin.client.settings.language;

import lombok.Getter;
import ru.avdonin.client.settings.BaseSettings;

@Getter
public abstract class BaseDictionary extends BaseSettings {
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
    private final String friends = "Friends";
    private final String requestFriends = "Friend requests";
    //Status bar
    private final String changeUser = "Change User";
    private final String restart = "Reboot";
    //Friend window
    private final String addFriendTitle = "Add friend";
    private final String rmFriendTitle = "Remove friend";
    private final String friendName = "Friend name";
    private final String deleteFriend = "Delete a friend";
    //Confirm friend window
    private final String confirmFriendTitle = "Request confirmation";
    private final String confirmFriend = "Accept the request";
    private final String rejectedFriend = "Reject the request";
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
    private final String january = "January";
    private final String february = "February";
    private final String march = "March";
    private final String april = "April";
    private final String may = "May";
    private final String june = "June";
    private final String july = "July";
    private final String august = "August";
    private final String september = "September";
    private final String october = "October";
    private final String november = "November";
    private final String december = "December";
    //Weekday
    private final String monday = "Mon";
    private final String tuesday = "Tue";
    private final String wednesday = "Wed";
    private final String thursday = "Thu";
    private final String friday = "Fri";
    private final String saturday = "Sat";
    private final String sunday = "Sun";
    //Confirmation
    private final String unconfirmed = "unconfirmed";
    private final String confirmed = "confirmed";
    private final String rejected = "rejected";
    //Default symbols
    private final String plus = "+";
    private final String minus = "-";
    private final String settings = "⚙";
    private final String ellipsis = "...";
}
