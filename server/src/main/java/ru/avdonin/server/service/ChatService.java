package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.*;
import ru.avdonin.server.repository.ChatParticipantRepository;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.chat.dto.ChatCreateDto;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.chat.dto.ChatRenameDto;
import ru.avdonin.template.model.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final FtpService ftpService;

    public void createChat(ChatCreateDto chatCreateDto) {
        User user = getUser(chatCreateDto.getUsername());

        Chat chat = Chat.builder()
                .id(generateChatId())
                .chatName(chatCreateDto.getChatName())
                .admin(user)
                .build();
        chat = chatRepository.save(chat);

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(chatParticipantID)
                .chat(chat)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
        ChatDto.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .admin(chat.getAdmin().getUsername())
                .customName(chat.getChatName())
                .build();
    }

    public void addUser(String chatId, String username) {
        Chat chat = getChat(chatId);
        User user = getUser(username);

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .id(chatParticipantID)
                .chat(chat)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<UserDto> getChatUsers(String chatId) {
        Chat chat = getChat(chatId);
        return chatParticipantRepository.findAllParticipant(chat.getId()).stream()
                .map(ChatParticipant::getUser)
                .map(this::getUserDtoFromUser)
                .toList();
    }

    public void deleteChat(String chatId, String username) {
        try {
            User user = getUser(username);
            Chat chat = getChat(chatId);
            if (!chat.getAdmin().equals(user))
                throw new IncorrectUserDataException("Only the admin can delete the chat");

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
                                .build())
                .toList();
    }

    public void logoutOfChat(String chatId, String username) {
        chatParticipantRepository.logoutOfChat(chatId, username);
    }

    public void renameChatCustom(ChatRenameDto chatRenameDto) {
        String chatId = chatRenameDto.getChatId();
        String username = chatRenameDto.getUsername();
        String newChatName = chatRenameDto.getNewChatName();

        Chat chat = getChat(chatId);
        User user = getUser(username);

        ChatParticipantID chatParticipantID = new ChatParticipantID(chat.getId(), user.getId());
        ChatParticipant chatParticipant = chatParticipantRepository.findById(chatParticipantID)
                .orElseThrow(() -> new IncorrectChatDataException("User " + username + " is not a member of chat " + chat.getChatName()));

        chatParticipant.setCustomChatName(newChatName);
        chatParticipantRepository.save(chatParticipant);
    }

    public void renameChatAdmin(ChatRenameDto chatRenameDto) {
        String chatId = chatRenameDto.getChatId();
        String username = chatRenameDto.getUsername();
        String newChatName = chatRenameDto.getNewChatName();

        Chat chat = getChat(chatId);
        User user = getUser(username);

        if (!chat.getAdmin().equals(user))
            throw new IncorrectUserDataException("Only the admin can rename the chat");

        chat.setChatName(newChatName);
        chatRepository.save(chat);
    }

    private String generateChatId() {
        return LocalDateTime.now().toString();
    }

    private Chat getChat(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IncorrectChatDataException("The chat does not exist"));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException("User " + username + " does not exist"));
    }

    private UserDto getUserDtoFromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .icon(ftpService.getIcon(user.getIcon()))
                .build();
    }
}
