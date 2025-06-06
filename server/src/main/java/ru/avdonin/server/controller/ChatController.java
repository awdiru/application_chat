package ru.avdonin.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.service.ChatService;
import ru.avdonin.server.service.MessageService;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.chat.dto.ChatCreateDto;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.chat.dto.ChatGetHistoryDto;
import ru.avdonin.template.model.chat.dto.ChatRenameDto;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController extends AbstractController {
    private final ChatService chatService;
    private final MessageService messageService;

    @Autowired
    public ChatController(Logger log, ChatService chatService, MessageService messageService) {
        super(log);
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createChat(@RequestBody ChatCreateDto chatCreateDto) {
        try {
            log.info("chatName: " + chatCreateDto.getChatName() + ", username: " + chatCreateDto.getUsername());
            chatService.createChat(chatCreateDto);
            return getOkResponse("The chat was created successfully");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addUser(@RequestParam String chatId,
                                          @RequestParam String username) {
        try {
            log.info("user " + username + ", chat id: " + chatId);
            chatService.addUser(chatId, username);
            return getOkResponse("User " + username + " has been added to the chat");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/users")
    public ResponseEntity<Object> getUsers(@RequestParam String chatId) {
        try {
            log.info("chat id: " + chatId);
            List<UserDto> users = chatService.getChatUsers(chatId);
            return ResponseEntity.ok().body(users);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/history")
    public ResponseEntity<Object> getChatHistory(@RequestBody ChatGetHistoryDto chatGetHistoryDto) {
        try {
            log.info("chat id " + chatGetHistoryDto.getChatId()
                    + ", from " + chatGetHistoryDto.getFrom()
                    + ", size " + chatGetHistoryDto.getSize());
            List<MessageDto> messages = messageService.getMessages(chatGetHistoryDto);
            return ResponseEntity.ok().body(messages);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<Object> getChats(@RequestParam String username) {
        try {
            log.info("username: " + username);
            List<ChatDto> chats = chatService.getChats(username);
            return ResponseEntity.ok().body(chats);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @DeleteMapping("/delete/chat")
    public ResponseEntity<Object> deleteChat(@RequestParam String chatId,
                                             @RequestParam String username) {
        try {
            log.info("chat id: " + chatId);
            chatService.deleteChat(chatId, username);
            return getOkResponse("Chat deleted");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PutMapping("/logout")
    public ResponseEntity<Object> logoutOfChat(@RequestParam String username,
                                               @RequestParam String chatId) {
        try {
            log.info("username: " + username + ", chat id: " + chatId);
            chatService.logoutOfChat(chatId, username);
            return getOkResponse("The user logged out of the chat");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PutMapping("/rename")
    public ResponseEntity<Object> renameChat(@RequestBody ChatRenameDto chatRenameDto) {
        try {
            log.info("username: " + chatRenameDto.getUsername() + ", chat id: " + chatRenameDto.getChatId());
            chatService.renameChatAdmin(chatRenameDto);
            return getOkResponse("Chat " + chatRenameDto.getChatId()
                    + " has been renamed to chat " + chatRenameDto.getNewChatName());
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PutMapping("/rename/custom")
    public ResponseEntity<Object> renameChatCustom(@RequestBody ChatRenameDto chatRenameDto) {
        try {
            log.info("username: " + chatRenameDto.getUsername() + ", chat id: " + chatRenameDto.getChatId());
            chatService.renameChatCustom(chatRenameDto);
            return getOkResponse("Chat " + chatRenameDto.getChatId()
                    + " has been renamed to chat " + chatRenameDto.getNewChatName()
                    + " for user " + chatRenameDto.getUsername());
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
