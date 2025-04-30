package ru.avdonin.testWebSocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.avdonin.testWebSocket.model.dto.message.MessageDto;
import ru.avdonin.testWebSocket.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageController {
    private final MessageService messageService;

    @GetMapping("get")
    public List<MessageDto> getChat(@RequestParam String sender,
                                    @RequestParam String recipient,
                                    @RequestParam int from,
                                    @RequestParam int size) {

        log("getChat: sender " + sender + ", recipient " + recipient);
        return messageService.getMessages(sender, recipient, from, size);
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] MessageController: " + text);
    }
}
