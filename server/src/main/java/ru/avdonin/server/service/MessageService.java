package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.server.model.Message;
import ru.avdonin.server.model.User;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageDto saveMessage(MessageDto messageDto) {
        log("saveMessage: messageDto: " + messageDto);
        User sender = userRepository.findByUsername(messageDto.getSender())
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + messageDto.getSender() + " does not exist"));
        User recipient = userRepository.findByUsername(messageDto.getRecipient())
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + messageDto.getRecipient() + " does not exist"));

        Message message = Message.builder()
                .time(LocalDateTime.now())
                .sender(sender)
                .recipient(recipient)
                .content(messageDto.getContent())
                .build();

        Message saved = messageRepository.save(message);
        return MessageDto.builder()
                .time(saved.getTime())
                .content(saved.getContent())
                .sender(saved.getSender().getUsername())
                .recipient(saved.getRecipient().getUsername())
                .build();
    }

    public List<MessageDto> getMessages (String sender, String recipient, int from, int size) {
        return messageRepository.findAllMessagesUsers(sender, recipient, PageRequest.of(from, size)).stream()
                .map(message -> MessageDto.builder()
                        .time(message.getTime())
                        .content(message.getContent())
                        .sender(message.getSender().getUsername())
                        .recipient(message.getRecipient().getUsername())
                        .build())
                .sorted((message1, message2) -> {
                    if (message1.getTime().isBefore(message2.getTime()))
                        return -1;
                    return 1;
                })
                .toList();
    }

    private void log (String text) {
        System.out.println("[" + LocalDateTime.now() + "] MessageService: " + text);
    }
}
