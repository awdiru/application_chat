package ru.avdonin.server.service.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.avdonin.server.entity_model.Chat;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.service.AbstractService;
import ru.avdonin.template.exceptions.IncorrectChatDataException;
import ru.avdonin.template.exceptions.IncorrectDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.server.entity_model.Message;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.model.chat.dto.ChatGetHistoryDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.message.dto.NewMessageDto;

import java.io.IOException;
import java.time.*;
import java.util.*;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageService extends AbstractService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final ChatRepository chatRepository;
    private final ImageFtpService imageFtpService;
    private final MessageHandler messageHandler;
    private final Map<String, List<Message>> chatsMessages = new HashMap<>();
    private final Map<String, String> messagesImages = new HashMap<>();

    public void saveMessage(MessageDto messageDto) throws IOException {

        User sender = userRepository.findByUsername(messageDto.getSender())
                .orElseThrow(() -> new IncorrectUserDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectUserDataException(messageDto.getSender())));
        Chat chat = chatRepository.findChatById(messageDto.getChatId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IncorrectChatDataException(getDictionary(messageDto.getLocale())
                        .getSaveMessageIncorrectChatDataException()));

        boolean isFilesAttached = messageDto.getImagesBase64() != null
                && !messageDto.getImagesBase64().isEmpty();
        StringBuilder fileNamesBuilder = null;

        if (isFilesAttached) {
            fileNamesBuilder = new StringBuilder();
            for (int i = 0; i < messageDto.getImagesBase64().size(); i++) {
                String receivedImage = messageDto.getImagesBase64().get(i);
                String fileName = getFileName();
                fileNamesBuilder.append(i == 0 ? fileName : "," + fileName);

                imageFtpService.upload(messageDto.getChatId(), fileName, receivedImage);
                messagesImages.put(fileName, receivedImage);
            }
        }

        Message message = Message.builder()
                .time(messageDto.getTime().toInstant())
                .content(encryptionService.encrypt(messageDto.getTextContent(), sender.getUsername(), messageDto.getLocale()))
                .sender(sender)
                .chat(chat)
                .fileName(fileNamesBuilder == null ? null : fileNamesBuilder.toString())
                .build();

        Message saved = messageRepository.save(message);

        NewMessageDto newMessageDto = NewMessageDto.builder()
                .messageId(saved.getId())
                .sender(sender.getUsername())
                .chatId(chat.getId())
                .build();
        messageHandler.sendToUsers(newMessageDto);
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

    public MessageDto getMessage(NewMessageDto newMessageDto) {
        Message message = messageRepository.findById(newMessageDto.getMessageId())
                .orElseThrow(() -> new IncorrectDataException("The message with id " + newMessageDto.getMessageId() + " was not found"));
        return getMessageDto(message, newMessageDto.getLocale());
    }

    private MessageDto getMessageDto(Message message, String locale) {
        List<String> imagesBase64 = null;

        if (message.getFileName() != null) {
            String[] imageNames = message.getFileName().split(",");
            imagesBase64 = new ArrayList<>(imageNames.length);

            for (String imageName : imageNames) {
                String imageBase64 = messagesImages.computeIfAbsent(imageName,
                        k -> imageFtpService.download(message.getChat().getId(), imageName));
                imagesBase64.add(imageBase64);
            }
        }

        return MessageDto.builder()
                .time(message.getTime().atOffset(ZoneOffset.UTC))
                .textContent(
                        encryptionService.decrypt(
                                message.getContent(),
                                message.getSender().getUsername(),
                                locale
                        ))
                .sender(message.getSender().getUsername())
                .chatId(message.getChat().getId())
                .imagesBase64(imagesBase64)
                .build();
    }

    private String getFileName() {
        return LocalDateTime.now() + ".png";
    }
}


