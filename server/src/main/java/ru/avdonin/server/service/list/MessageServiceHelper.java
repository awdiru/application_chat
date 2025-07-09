package ru.avdonin.server.service.list;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.repository.MessageRepository;
import ru.avdonin.template.model.chat.dto.ChatIdDto;
import ru.avdonin.template.model.message.dto.BaseMessageDto;
import ru.avdonin.template.model.message.dto.ForwardedMessageDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.message.dto.NewMessageDto;
import ru.avdonin.template.model.util.ActionNotification;
import ru.avdonin.template.model.util.actions.Actions;
import ru.avdonin.template.model.util.actions.BaseData;
import ru.avdonin.template.model.util.actions.list.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageServiceHelper {
    private final MessageRepository messageRepository;
    private final MessageHandler messageHandler;

    @Transactional
    public void readMessage(ChatIdDto chatIdDto) {
        messageRepository.readMessages(chatIdDto.getChatId());
    }

    public void sendToUsers(BaseData data) throws IOException {
        Actions action = getAction(data);

        ActionNotification<?> actionNotification = ActionNotification.builder()
                .action(action)
                .data(data)
                .build();

        messageHandler.sendToUsersMessage(actionNotification);
    }

    private Actions getAction(BaseData data) {
        if (data instanceof MessageAct) return Actions.MESSAGE;
        if (data instanceof ForwardAct) return Actions.FORWARD;
        if (data instanceof InvitationAct) return Actions.INVITATION;
        if (data instanceof ReadAct) return Actions.READ;
        if (data instanceof TypingAct) return Actions.TYPING;
        return null;
    }

    public <M extends BaseMessageDto> MessageDto.Type getType(M message) {
        if (message instanceof NewMessageDto) return MessageDto.Type.MESSAGE;
        if (message instanceof ForwardedMessageDto) return MessageDto.Type.FORWARD;
        return null;
    }
}
