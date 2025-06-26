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
import ru.avdonin.template.model.util.ActionNotification;

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

        String fileNames = getMessageFileNamesAndSaveFiles(messageDto);

        Message message = Message.builder()
                .time(messageDto.getTime().toInstant())
                .textContent(encryptionService.encrypt(messageDto.getTextContent(), sender.getUsername(), messageDto.getLocale()))
                .sender(sender)
                .chat(chat)
                .fileNames(fileNames)
                .build();

        Message saved = messageRepository.save(message);

        ActionNotification actionNotification = ActionNotification.builder()
                .action(ActionNotification.Action.MESSAGE)
                .data(ActionNotification.Message.builder()
                                .messageId(message.getId())
                                .sender(sender.getUsername())
                                .chatId(chat.getId())
                                .locale(messageDto.getLocale())
                                .build())
                .build();
        messageHandler.sendToUsersMessageNotification(actionNotification);
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

    public MessageDto getMessage(MessageDto messageDto) {
        Message message = messageRepository.findById(messageDto.getId())
                .orElseThrow(() -> new IncorrectDataException("The message with id " + messageDto.getId() + " was not found"));

        return getMessageDto(message, messageDto.getLocale());
    }

    public void changeMessage(MessageDto messageDto) {
        Message message = messageRepository.findById(messageDto.getId())
                .orElseThrow(() -> new IncorrectChatDataException("The message with id " + messageDto.getId() + " was not found"));

        if (!message.getSender().getUsername().equals(messageDto.getSender()))
            throw  new IncorrectUserDataException("Only the author of the message can change the messages");

        message.setTextContent(encryptionService.encrypt(messageDto.getTextContent(), messageDto.getSender(), messageDto.getLocale()));
        message.setFileNames(getMessageFileNamesAndSaveFiles(messageDto));
        messageRepository.save(message);
    }

    private MessageDto getMessageDto(Message message, String locale) {
        Set<String> imagesBase64 = null;

        if (message.getFileNames() != null) {
            String[] imageNames = message.getFileNames().split(",");
            imagesBase64 = new HashSet<>(imageNames.length);

            for (String imageName : imageNames) {
                String imageBase64 = messagesImages.computeIfAbsent(imageName,
                        k -> imageFtpService.download(message.getChat().getId(), imageName));
                imagesBase64.add(imageBase64);
            }
        }

        return MessageDto.builder()
                .id(message.getId())
                .time(message.getTime().atOffset(ZoneOffset.UTC))
                .textContent(
                        encryptionService.decrypt(
                                message.getTextContent(),
                                message.getSender().getUsername(),
                                locale
                        ))
                .sender(message.getSender().getUsername())
                .chatId(message.getChat().getId())
                .imagesBase64(imagesBase64)
                .build();
    }

    private String getMessageFileNamesAndSaveFiles(MessageDto messageDto) {
        StringBuilder fileNamesBuilder = null;
        boolean isFilesAttached = messageDto.getImagesBase64() != null
                && !messageDto.getImagesBase64().isEmpty();

        if (isFilesAttached) {
            fileNamesBuilder = new StringBuilder();
            for (String imageBase64 : messageDto.getImagesBase64()) {
                String fileName = getFileName();
                fileNamesBuilder.append(fileName).append(",");

                imageFtpService.upload(messageDto.getChatId(), fileName, imageBase64);
                messagesImages.put(fileName, imageBase64);
            }
            fileNamesBuilder.delete(fileNamesBuilder.length() - 1, fileNamesBuilder.length());
        }
        return fileNamesBuilder == null ? null : fileNamesBuilder.toString();
    }

    private String getFileName() {
        return LocalDateTime.now() + ".png";
    }
}


