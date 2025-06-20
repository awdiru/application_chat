package ru.avdonin.server.service.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.Chat;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.service.AbstractService;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.server.entity_model.Message;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.model.chat.dto.ChatGetHistoryDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.time.*;
import java.util.*;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageService extends AbstractService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final ChatRepository chatRepository;
    private final AvatarFtpService avatarFtpService;
    private final Map<String, List<Message>> chatsMessages = new HashMap<>();

    public MessageDto saveMessage(MessageDto messageDto) {

        User sender = userRepository.findByUsername(messageDto.getSender())
                .orElseThrow(() -> new IncorrectUserDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectUserDataException(messageDto.getSender())));
        Chat chat = chatRepository.findChatById(messageDto.getChatId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectChatDataException()));

        Message saved = Message.builder()
                .time(Instant.now())
                .content(encryptionService.encrypt(messageDto.getContent(), sender.getUsername(), messageDto.getLocale()))
                .sender(sender)
                .chat(chat)
                .build();

        List<Message> messages = chatsMessages.computeIfAbsent(chat.getId(), k -> new ArrayList<>());
        messages.add(saved);
        if (messages.size() >= 5) {
            messageRepository.saveAll(messages);
            messages.clear();
        }

        return MessageDto.builder()
                .time(saved.getTime().atOffset(ZoneOffset.UTC))
                .content(messageDto.getContent())
                .sender(messageDto.getSender())
                .chatId(messageDto.getChatId())
                .build();
    }

    public List<MessageDto> getMessages(ChatGetHistoryDto chatGetHistoryDto) {
        int from = chatGetHistoryDto.getFrom();
        List<Message> history = messageRepository.findAllMessagesChat(chatGetHistoryDto.getChatId(),
                PageRequest.of(from, chatGetHistoryDto.getSize()));

        if (from == 0) {
            List<Message> unsavedHistory = chatsMessages.computeIfAbsent(chatGetHistoryDto.getChatId(), k -> new ArrayList<>());
            history.addAll(unsavedHistory);
        }

        return history.stream()
                .map(message -> getMessageDto(message, chatGetHistoryDto.getLocale()))
                .sorted(Comparator.comparing(MessageDto::getTime))
                .toList();
    }

    private MessageDto getMessageDto(Message message, String locale) {
        return MessageDto.builder()
                .time(message.getTime().atOffset(ZoneOffset.UTC))
                .content(
                        encryptionService.decrypt(
                                message.getContent(),
                                message.getSender().getUsername(),
                                locale
                        ))
                .sender(message.getSender().getUsername())
                .chatId(message.getChat().getId())
                .file(null)
                .avatar(avatarFtpService.downloadAvatar(message.getSender().getUsername(), message.getSender().getAvatarFileName()))
                .build();
    }
}


