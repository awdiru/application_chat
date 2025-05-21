package ru.avdonin.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.avdonin.server.service.MessageService;
import ru.avdonin.template.model.message.dto.MessageDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.avdonin.template.model.util.ResponseBuilder.getErrorResponse;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/get")
    public ResponseEntity<Object> getChat(@RequestParam String sender,
                                          @RequestParam String recipient,
                                          @RequestParam int from,
                                          @RequestParam int size) {

        try {
            log("getChat: sender " + sender + ", recipient " + recipient);
            List<MessageDto> messages = messageService.getMessages(sender, recipient, from, size);
            return ResponseEntity.ok().body(messages);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] MessageController: " + text);
    }
}
