package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.Chat;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.server.entity_model.Message;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.model.chat.dto.ChatGetHistoryDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.time.*;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageService extends AbstractService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final ChatRepository chatRepository;
    private final FtpService ftpService;

    public MessageDto saveMessage(MessageDto messageDto) {
        User sender = userRepository.findByUsername(messageDto.getSender())
                .orElseThrow(() -> new IncorrectUserDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectUserDataException(messageDto.getSender())));
        Chat chat = chatRepository.findChatById(messageDto.getChat())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectChatDataException()));

        Message message = Message.builder()
                .time(Instant.now())
                .content(encryptionService.encrypt(messageDto.getContent(), messageDto.getLocale()))
                .sender(sender)
                .chat(chat)
                .build();

        Message saved = messageRepository.save(message);
        return MessageDto.builder()
                .time(saved.getTime().atOffset(ZoneOffset.UTC))
                .content(messageDto.getContent())
                .sender(messageDto.getSender())
                .chat(messageDto.getChat())
                .build();
    }

    public List<MessageDto> getMessages(ChatGetHistoryDto chatGetHistoryDto) {
        return messageRepository.findAllMessagesChat(chatGetHistoryDto.getChatId(), PageRequest.of(chatGetHistoryDto.getFrom(), chatGetHistoryDto.getSize())).stream()
                .map(message -> MessageDto.builder()
                        .time(message.getTime().atOffset(ZoneOffset.UTC))
                        .content(encryptionService.decrypt(message.getContent(), chatGetHistoryDto.getLocale()))
                        .sender(message.getSender().getUsername())
                        .chat(message.getChat().getId())
                        .file(ftpService.getFile(message.getFile()))
                        .build())
                .sorted(Comparator.comparing(MessageDto::getTime))
                .toList();
    }
}


