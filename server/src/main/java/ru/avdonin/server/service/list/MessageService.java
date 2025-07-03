package ru.avdonin.server.service.list;

import jakarta.transaction.Transactional;
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
import ru.avdonin.template.model.chat.dto.ChatIdDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.message.dto.UnreadMessagesCountDto;
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
    private final MessageServiceHelper messageServiceHelper;
    private final Map<String, List<Message>> chatsMessages = new HashMap<>();
    private final Map<String, String> messagesImages = new HashMap<>();

    @Transactional
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
                .edited(false)
                .isRead(false)
                .build();

        messageRepository.save(message);

        ActionNotification actionNotification = ActionNotification.builder()
                .action(ActionNotification.Action.MESSAGE)
                .data(ActionNotification.Message.builder()
                        .messageId(message.getId())
                        .sender(sender.getUsername())
                        .chatId(chat.getId())
                        .build())
                .build();
        messageHandler.sendToUsersMessage(actionNotification);
    }

    public List<MessageDto> getMessages(ChatGetHistoryDto chatGetHistoryDto) {
        int from = chatGetHistoryDto.getFrom();
        String chatId = chatGetHistoryDto.getChatId();

        List<Message> history = messageRepository.findAllMessagesChat(chatId,
                PageRequest.of(from, chatGetHistoryDto.getSize()));

        messageServiceHelper.readMessage(new ChatIdDto(chatId, chatGetHistoryDto.getLocale()));

        if (from == 0) {
            List<Message> unsavedHistory = chatsMessages.computeIfAbsent(chatId, k -> new ArrayList<>())
                    .stream()
                    .peek(m -> m.setIsRead(true))
                    .toList();
            chatsMessages.put(chatId, unsavedHistory);
            history.addAll(unsavedHistory);
        }

        return history.stream()
                .map(message -> getMessageDto(message, chatGetHistoryDto.getLocale()))
                .sorted(Comparator.comparing(MessageDto::getTime))
                .toList();
    }

    public MessageDto getMessage(MessageDto messageDto) {
        Message message = getMessageOrException(messageDto);
        return getMessageDto(message, messageDto.getLocale());
    }

    public void changeMessage(MessageDto messageDto) {
        Message message = getMessageOrException(messageDto);

        if (!message.getSender().getUsername().equals(messageDto.getSender()))
            throw new IncorrectUserDataException("Only the author of the message can change the messages");

        message.setTextContent(encryptionService.encrypt(messageDto.getTextContent(), messageDto.getSender(), messageDto.getLocale()));
        String filesNames = getMessageFileNamesAndSaveFiles(messageDto);
        if (filesNames != null) message.setFileNames(filesNames);
        message.setEdited(true);
        messageRepository.save(message);
    }

    public void deleteMessage(MessageDto messageDto) {
        Message message = getMessageOrException(messageDto);

        if (!message.getSender().getUsername().equals(messageDto.getSender()))
            throw new IncorrectUserDataException("Only the author of the message can deleted the messages");

        messageRepository.delete(message);
    }

    public void readMessage(ChatIdDto chatIdDto) {
        try {
            messageServiceHelper.readMessage(chatIdDto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UnreadMessagesCountDto getUnreadMessagesCount(ChatIdDto chatIdDto) {
        long unreadMessagesCount = messageRepository.getUnreadMessagesCount(chatIdDto.getChatId());
        return UnreadMessagesCountDto.builder()
                .chatId(chatIdDto.getChatId())
                .unreadMessagesCount(unreadMessagesCount)
                .locale(chatIdDto.getLocale())
                .build();
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
                .edited(message.getEdited())
                .read(true)
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

    private Message getMessageOrException(MessageDto messageDto) {
        if (messageDto.getId() != null)
            return messageRepository.findById(messageDto.getId())
                    .orElseThrow(() -> new IncorrectDataException("The message with id " + messageDto.getId() + " was not found"));

        List<Message> messages = messageRepository.findAllByTime(messageDto.getTime().toInstant());
        if (messages == null || messages.isEmpty())
            throw new IncorrectDataException("The message not found");

        for (Message message : messages) {
            MessageDto respMessage = getMessageDto(message, messageDto.getLocale());
            if (respMessage.equals(messageDto)) return message;
        }
        throw new IncorrectDataException("The message not found");
    }

    private String getFileName() {
        return LocalDateTime.now() + ".png";
    }
}


