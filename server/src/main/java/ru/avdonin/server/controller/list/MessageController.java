package ru.avdonin.server.controller.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.controller.AbstractController;
import ru.avdonin.server.service.list.MessageService;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.message.dto.MessageDto;

@RestController
@RequestMapping("/message")
public class MessageController extends AbstractController {
    private final MessageService messageService;

    @Autowired
    public MessageController(Logger log, MessageService messageService) {
        super(log);
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<Object> sendMessage(@RequestBody MessageDto messageDto) {
        try {
            log.info("send message: " + messageDto.getChatId());
            messageService.saveMessage(messageDto);
            return getOkResponse("The message has been sent");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getMessage(@RequestBody MessageDto messageDto) {
        try {
            log.info("get message: " + messageDto.getId());
            MessageDto responseDto = messageService.getMessage(messageDto);
            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/change")
    public ResponseEntity<Object> changeMessage(@RequestBody MessageDto messageDto) {
        try {
            log.info("change message: " + messageDto.getId());
            messageService.changeMessage(messageDto);
            return getOkResponse("The message has been changed");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
