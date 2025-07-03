package ru.avdonin.server.service.list;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.template.model.chat.dto.ChatIdDto;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageServiceHelper {
    private final MessageRepository messageRepository;

    @Transactional
    public void readMessage(ChatIdDto chatIdDto) {
        try {
            messageRepository.readMessages(chatIdDto.getChatId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
