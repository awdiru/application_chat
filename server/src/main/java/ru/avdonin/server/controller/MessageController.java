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
import ru.avdonin.template.model.util.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

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
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "getChat");
        }
    }

    private ResponseEntity<Object> errorHandler(Exception e, HttpStatus status, String method) {
        log(method + ": ERROR: " + e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), status, e.getMessage()), status);
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] MessageController: " + text);
    }
}
