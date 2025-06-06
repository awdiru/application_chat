package ru.avdonin.server.service.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.Chat;
import ru.avdonin.server.entity_model.ChatParticipant;
import ru.avdonin.server.entity_model.ChatParticipantID;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.ChatParticipantRepository;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.server.service.AbstractService;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.user.dto.UserDto;
import ru.avdonin.template.model.user.dto.UserFriendDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ChatService extends AbstractService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final FtpService ftpService;

    public void createPublicChat(ChatCreateDto chatCreateDto) {
        if (chatCreateDto.getChatName() == null || chatCreateDto.getChatName().isEmpty())
            throw new IncorrectChatDataException(getDictionary(chatCreateDto.getLocale())
                    .getCreateChatIncorrectChatDataException());

        User user = getUser(chatCreateDto.getUsername(), chatCreateDto.getLocale());

        Chat chat = Chat.builder()
                .id(generateChatId())
                .chatName(chatCreateDto.getChatName())
                .admin(user)
                .privateChat(false)
                .build();
        chat = chatRepository.save(chat);

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(chatParticipantID)
                .chat(chat)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public void createPrivateChat(ChatCreateDto chatCreateDto) {
        if (chatCreateDto.getChatName() == null || chatCreateDto.getChatName().isEmpty())
            throw new IncorrectChatDataException(
                    getDictionary(chatCreateDto.getLocale()).getCreateChatIncorrectChatDataException());

        User user = getUser(chatCreateDto.getUsername(), chatCreateDto.getLocale());
        User friend = getUser(chatCreateDto.getChatName(), chatCreateDto.getLocale());

        Chat chatOld = null;

        try {
            chatOld = getPrivateChat(user.getUsername(), friend.getUsername(), chatCreateDto.getLocale());
        } catch (Exception ignored) {
        }

        if (chatOld != null) throw new IncorrectChatDataException(
                getDictionary(chatCreateDto.getLocale()).getCreatePrivateChatIncorrectChatDataException());

        Chat chat = Chat.builder()
                .id(generateChatId())
                .chatName(chatCreateDto.getUsername() + " - " + chatCreateDto.getChatName())
                .admin(user)
                .privateChat(true)
                .build();
        chat = chatRepository.save(chat);

        ChatParticipantID chatParticipantID1 = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant1 = ChatParticipant.builder()
                .id(chatParticipantID1)
                .chat(chat)
                .user(user)
                .customChatName(chatCreateDto.getChatName())
                .build();
        chatParticipantRepository.save(chatParticipant1);

        ChatParticipantID chatParticipantID2 = new ChatParticipantID(chat.getId(), friend.getId());
        ChatParticipant chatParticipant2 = ChatParticipant.builder()
                .id(chatParticipantID2)
                .chat(chat)
                .user(friend)
                .customChatName(chatCreateDto.getUsername())
                .build();
        chatParticipantRepository.save(chatParticipant2);
    }

    public void createPersonalChat(ChatCreateDto chatCreateDto) {
        if (chatCreateDto.getChatName() == null || chatCreateDto.getChatName().isEmpty())
            throw new IncorrectChatDataException(
                    getDictionary(chatCreateDto.getLocale()).getCreateChatIncorrectChatDataException());

        User user = getUser(chatCreateDto.getUsername(), chatCreateDto.getLocale());

        Chat chat = Chat.builder()
                .id(generateChatId())
                .chatName(chatCreateDto.getUsername())
                .admin(user)
                .privateChat(true)
                .build();
        chat = chatRepository.save(chat);

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(chatParticipantID)
                .chat(chat)
                .user(user)
                .customChatName(getDictionary(chatCreateDto.getLocale()).getPersonal())
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public void addUser(ChatParticipantDto chatParticipantDto) {
        Chat chat = getChat(chatParticipantDto.getChatId(), chatParticipantDto.getLocale());
        User user = getUser(chatParticipantDto.getUsername(), chatParticipantDto.getLocale());

        if (chat.getPrivateChat())
            throw new IncorrectChatDataException(getDictionary(chatParticipantDto.getLocale())
                    .getAddUserIncorrectChatDataException());

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(chatParticipantID)
                .chat(chat)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<UserDto> getChatUsers(ChatIdDto chatIdDto) {
        Chat chat = getChat(chatIdDto.getChatId(), chatIdDto.getLocale());
        return chatParticipantRepository.findAllParticipant(chat.getId()).stream()
                .map(ChatParticipant::getUser)
                .map(this::getUserDtoFromUser)
                .toList();
    }

    public void deleteChat(ChatParticipantDto chatParticipantDto) {
        try {
            User user = getUser(chatParticipantDto.getUsername(), chatParticipantDto.getLocale());
            Chat chat = getChat(chatParticipantDto.getChatId(), chatParticipantDto.getLocale());
            if (!chat.getAdmin().equals(user))
                throw new IncorrectUserDataException(getDictionary(chatParticipantDto.getLocale())
                        .getDeleteChatIncorrectUserDataException());

            chatRepository.delete(chat);
        } catch (IncorrectChatDataException ignored) {
        }
    }

    public List<ChatDto> getChats(String username) {
        return chatParticipantRepository.findAllChatsUser(username).stream()
                .map(cp ->
                        ChatDto.builder()
                                .id(cp.getChat().getId())
                                .chatName(cp.getChat().getChatName())
                                .admin(cp.getChat().getAdmin().getUsername())
                                .customName(cp.getCustomChatName())
                                .privateChat(cp.getChat().getPrivateChat())
                                .build())
                .toList();
    }

    public void logoutOfChat(ChatParticipantDto chatParticipantDto) {
        User user = getUser(chatParticipantDto.getUsername(), chatParticipantDto.getLocale());
        Chat chat = getChat(chatParticipantDto.getChatId(), chatParticipantDto.getLocale());

        if (chat.getPrivateChat()) chatRepository.deleteById(chat.getId());
        else chatParticipantRepository.deleteById(new ChatParticipantID(chat.getId(), user.getId()));
    }

    public void renameChatCustom(ChatRenameDto chatRenameDto) {
        String chatId = chatRenameDto.getChatId();
        String username = chatRenameDto.getUsername();
        String newChatName = chatRenameDto.getNewChatName();

        Chat chat = getChat(chatId, chatRenameDto.getLocale());
        User user = getUser(username, chatRenameDto.getLocale());

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = chatParticipantRepository.findById(chatParticipantID)
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(chatRenameDto.getLocale())
                        .getRenameChatCustomIncorrectChatDataException(username, chat.getChatName())));

        chatParticipant.setCustomChatName(newChatName);
        chatParticipantRepository.save(chatParticipant);
    }

    public void renameChatAdmin(ChatRenameDto chatRenameDto) {
        Chat chat = getChat(chatRenameDto.getChatId(), chatRenameDto.getLocale());
        User user = getUser(chatRenameDto.getUsername(), chatRenameDto.getLocale());

        if (!chat.getAdmin().equals(user))
            throw new IncorrectUserDataException(getDictionary(chatRenameDto.getLocale())
                    .getRenameChatAdminIncorrectChatDataException());

        chat.setChatName(chatRenameDto.getNewChatName());
        chatRepository.save(chat);
    }

    public ChatDto getPrivateChat(UserFriendDto userFriendDto) {
        User user = getUser(userFriendDto.getUsername(), userFriendDto.getLocale());
        User friend = getUser(userFriendDto.getFriendName(), userFriendDto.getLocale());

        Chat chat = getPrivateChat(user.getUsername(), friend.getUsername(), userFriendDto.getLocale());

        return getChatDto(user, chat, userFriendDto.getLocale());
    }

    public ChatDto getPersonalChat(UserDto userDto) {
        User user = getUser(userDto.getUsername(), userDto.getLocale());
        Chat chat = chatRepository.findChatByChatName(userDto.getUsername(), userDto.getUsername()).stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(
                        getDictionary(userDto.getLocale()).getGetPrivateChatIncorrectChatDataException()));

        return getChatDto(user, chat, userDto.getLocale());
    }

    private ChatDto getChatDto(User user, Chat chat, String locale) {
        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = chatParticipantRepository.findById(chatParticipantID)
                .orElseThrow(() -> new IncorrectChatDataException(
                        getDictionary(locale).getGetPrivateChatIncorrectChatDataException()));

        return ChatDto.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .customName(chatParticipant.getCustomChatName())
                .privateChat(chat.getPrivateChat())
                .admin(chat.getAdmin().getUsername())
                .build();
    }

    private Chat getPrivateChat(String username, String friendName, String locale) {
        String chatName1 = username + " - " + friendName;
        String chatName2 = friendName + " - " + username;

        return chatRepository.findChatByChatName(chatName1, chatName2).stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(
                        getDictionary(locale).getGetPrivateChatIncorrectChatDataException()));
    }

    private String generateChatId() {
        return LocalDateTime.now().toString();
    }

    private Chat getChat(String chatId, String locale) {
        return chatRepository.findChatById(chatId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(locale)
                        .getGetChatIncorrectChatDataException()));
    }

    private User getUser(String username, String locale) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException(getDictionary(locale)
                        .getGetUserIncorrectUserDataException(username)));
    }

    private UserDto getUserDtoFromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .icon(ftpService.getIcon(user.getIcon()))
                .build();
    }
}
