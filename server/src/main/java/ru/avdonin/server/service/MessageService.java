package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.server.entity_model.Message;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.time.*;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public MessageDto saveMessage(MessageDto messageDto) {
        log("saveMessage: messageDto: " + messageDto);
        User sender = userRepository.findByUsername(messageDto.getSender())
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + messageDto.getSender() + " does not exist"));
        User recipient = userRepository.findByUsername(messageDto.getRecipient())
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + messageDto.getRecipient() + " does not exist"));

        Message message = Message.builder()
                .time(Instant.now())
                .sender(sender)
                .recipient(recipient)
                .content(encryptionService.encrypt(messageDto.getContent()))
                .build();

        Message saved = messageRepository.save(message);
        return MessageDto.builder()
                .time(saved.getTime().atOffset(ZoneOffset.UTC))
                .content(messageDto.getContent())
                .sender(messageDto.getSender())
                .recipient(messageDto.getRecipient())
                .build();
    }

    public List<MessageDto> getMessages (String sender, String recipient, int from, int size) {
        return messageRepository.findAllMessagesUsers(sender, recipient, PageRequest.of(from, size)).stream()
                .map(message -> MessageDto.builder()
                        .time(message.getTime().atOffset(ZoneOffset.UTC))
                        .content(encryptionService.decrypt(message.getContent()))
                        .sender(message.getSender().getUsername())
                        .recipient(message.getRecipient().getUsername())
                        .build())
                .sorted(Comparator.comparing(MessageDto::getTime))
                .toList();
    }

    private void log (String text) {
        System.out.println("[" + LocalDateTime.now() + "] MessageService: " + text);
    }
}


