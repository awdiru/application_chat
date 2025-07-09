package ru.avdonin.server.controller.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.avdonin.server.service.list.MessageHandler;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.util.ActionNotification;
import ru.avdonin.template.model.util.TypingDto;

import ru.avdonin.server.controller.AbstractController;
import ru.avdonin.template.model.util.actions.list.TypingAct;
import ru.avdonin.template.model.util.actions.Actions;


@RestController
public class TypingController extends AbstractController {
    private final MessageHandler messageHandler;

    @Autowired
    public TypingController(Logger log, MessageHandler messageHandler) {
        super(log);
        this.messageHandler = messageHandler;
    }

    @PostMapping("/typing")
    public ResponseEntity<Object> isTyping(@RequestBody TypingDto typingDto) {
        try {
            log.info("typing: " + typingDto.getUsername() + " " + typingDto.getIsTyping());
            ActionNotification actionNotification = ActionNotification.builder()
                    .action(Actions.TYPING)
                    .data(TypingAct.builder()
                            .chatId(typingDto.getChatId())
                            .username(typingDto.getUsername())
                            .isTyping(typingDto.getIsTyping())
                            .build())
                    .build();
            messageHandler.sendToUsersTyping(actionNotification);
            return getOkResponse("The message has been sent");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
