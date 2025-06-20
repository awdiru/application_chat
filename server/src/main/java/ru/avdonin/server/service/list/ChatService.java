package ru.avdonin.server.service.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.*;
import ru.avdonin.server.repository.ChatParticipantRepository;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.repository.InvitationsRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.server.service.AbstractService;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectInvitationChatException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.user.dto.UserDto;
import ru.avdonin.template.model.user.dto.UserFriendDto;
import ru.avdonin.template.model.user.dto.UsernameDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ChatService extends AbstractService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final InvitationsRepository invitationsRepository;
    private final FtpService ftpService;

    public ChatDto createPublicChat(ChatCreateDto chatCreateDto) {
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

        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                .chat(chat)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);

        return ChatDto.builder()
                .id(chat.getId())
                .build();
    }

    public ChatDto createPrivateChat(ChatCreateDto chatCreateDto) {
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

        ChatParticipant chatParticipant1 = ChatParticipant.builder()
                .id(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                .chat(chat)
                .user(user)
                .customChatName(chatCreateDto.getChatName())
                .build();
        chatParticipantRepository.save(chatParticipant1);

        ChatParticipant chatParticipant2 = ChatParticipant.builder()
                .id(new ChatParticipant.ChatParticipantID(chat.getId(), friend.getId()))
                .chat(chat)
                .user(friend)
                .customChatName(chatCreateDto.getUsername())
                .build();
        chatParticipantRepository.save(chatParticipant2);

        return ChatDto.builder()
                .id(chat.getId())
                .build();
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

        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                .chat(chat)
                .user(user)
                .customChatName(getDictionary(chatCreateDto.getLocale()).getPersonal())
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public void addUser(InvitationChatDto invitationChatDto) {
        Chat chat = getChat(invitationChatDto.getChatId(), invitationChatDto.getLocale());
        User user = getUser(invitationChatDto.getUsername(), invitationChatDto.getLocale());

        if (chat.getPrivateChat())
            throw new IncorrectChatDataException(getDictionary(invitationChatDto.getLocale())
                    .getAddUserIncorrectChatDataException());

        ChatParticipant chatParticipant = chatParticipantRepository
                .findById(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                .orElse(null);

        if (chatParticipant != null)
            throw new IncorrectInvitationChatException(getDictionary(invitationChatDto.getLocale())
                    .getAddUserIncorrectInvitationException(user.getUsername(), chat.getChatName()));

        InvitationChat invitationChat = InvitationChat.builder()
                .chat(chat)
                .user(user)
                .build();
        invitationsRepository.save(invitationChat);
    }

    public List<InvitationChatDto> getInvitations(UsernameDto usernameDto) {
        User user = getUser(usernameDto.getUsername(), usernameDto.getLocale());
        List<InvitationChat> invitationsChats = invitationsRepository.findAllByUsername(user.getUsername());
        return invitationsChats.stream()
                .map(inv -> InvitationChatDto.builder()
                        .chatId(inv.getChat().getId())
                        .username(inv.getUser().getUsername())
                        .chatName(inv.getChat().getChatName())
                        .build())
                .toList();
    }

    public InvitationChatDto confirmInvitation(InvitationChatDto invitationChatDto) {
        User user = getUser(invitationChatDto.getUsername(), invitationChatDto.getLocale());
        Chat chat = getChat(invitationChatDto.getChatId(), invitationChatDto.getLocale());
        InvitationChat invitationChat = invitationsRepository.findByUsernameAndChatId(user.getUsername(), chat.getId())
                .orElseThrow(() -> new IncorrectInvitationChatException(getDictionary(invitationChatDto.getLocale())
                        .getConfirmInvitationIncorrectInvitationChatException()));

        invitationsRepository.deleteById(invitationChat.getId());

        if (invitationChatDto.isConfirmed()) {
            ChatParticipant chatParticipant = ChatParticipant.builder()
                    .id(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                    .chat(chat)
                    .user(user)
                    .build();
            chatParticipantRepository.save(chatParticipant);
            return InvitationChatDto.builder()
                    .chatId(chat.getId())
                    .username(user.getUsername())
                    .confirmed(true)
                    .build();
        } else {
            return InvitationChatDto.builder()
                    .chatId(chat.getId())
                    .username(user.getUsername())
                    .build();
        }
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
        else chatParticipantRepository.deleteById(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()));
    }

    public void renameChatCustom(ChatRenameDto chatRenameDto) {
        String chatId = chatRenameDto.getChatId();
        String username = chatRenameDto.getUsername();
        String newChatName = chatRenameDto.getNewChatName();

        Chat chat = getChat(chatId, chatRenameDto.getLocale());
        User user = getUser(username, chatRenameDto.getLocale());

        ChatParticipant.ChatParticipantID chatParticipantID = new ChatParticipant.ChatParticipantID(chat.getId(), user.getId());
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
        ChatParticipant chatParticipant = getChatParticipant(chat, user, locale);
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

    private ChatParticipant getChatParticipant(Chat chat, User user, String locale) {
        return chatParticipantRepository.findById(new ChatParticipant.ChatParticipantID(chat.getId(), user.getId()))
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(locale)
                        .getChatParticipantIncorrectChatDataException(user.getUsername(), chat.getChatName())));
    }

    private UserDto getUserDtoFromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .icon(ftpService.getIcon(user.getIcon()))
                .build();
    }
}
