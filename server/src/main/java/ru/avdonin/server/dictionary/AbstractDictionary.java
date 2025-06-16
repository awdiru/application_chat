package ru.avdonin.server.dictionary;

import lombok.Getter;

@Getter
public abstract class AbstractDictionary {
    private final String addUserIncorrectChatDataException = "A user cannot be added to a private chat";
    private final String deleteChatIncorrectUserDataException = "Only the admin can delete the chat";
    private final String renameChatAdminIncorrectChatDataException = "Only the admin can rename the chat";
    private final String getChatIncorrectChatDataException = "The chat does not exist";
    private final String getSecretKeySpecIllegalArgumentException = "Invalid key. Must be 32 chars";
    private final String saveMessageIncorrectChatDataException = "This chat does not exist";
    private final String validateIncorrectUserDataException = "Invalid password";
    private final String saveIncorrectLoginException = "The username cannot be empty";
    private final String saveIncorrectPasswordException = "The password cannot be empty";
    private final String saveIncorrectUserDataException = "A user with that name has already been registered";
    private final String createChatIncorrectChatDataException = "The chat name cannot be empty";
    private final String getPrivateChatIncorrectChatDataException = "There is no chat submitted by the user";
    private final String createPrivateChatIncorrectChatDataException = "Such a private chat already exists";
    private final String confirmInvitationIncorrectInvitationChatException = "You were not invited to this chat";

    public String getSaveMessageIncorrectUserDataException(String sender) {
        return "User with username " + sender + " does not exist";
    }

    public String getRenameChatCustomIncorrectChatDataException(String username, String chatName) {
        return "User " + username + " is not a member of chat " + chatName;
    }

    public String getGetUserIncorrectUserDataException(String username) {
        return "User " + username + " does not exist";
    }

    public String getEncryptRuntimeException(String message) {
        return "Encryption failed: " + message;
    }

    public String getDecryptRuntimeException(String message) {
        return "Decryption failed: " + message;
    }

    public String getSearchUserByUsernameIncorrectUserDataException(String username) {
        return "User with username " + username + " does not exist";
    }

    private final String personal = "Personal";
}
