package ru.avdonin.server.controller.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.controller.AbstractController;
import ru.avdonin.server.service.list.ChatService;
import ru.avdonin.server.service.list.MessageService;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.chat.dto.*;
import ru.avdonin.template.model.message.dto.MessageDto;
import ru.avdonin.template.model.user.dto.UserDto;
import ru.avdonin.template.model.user.dto.UserFriendDto;
import ru.avdonin.template.model.user.dto.UsernameDto;

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
            ChatDto chatDto = chatCreateDto.getPrivateChat()
                    ? chatService.createPrivateChat(chatCreateDto)
                    : chatService.createPublicChat(chatCreateDto);
            return ResponseEntity.ok().body(chatDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addUser(@RequestBody InvitationChatDto invitationChatDto) {
        try {
            log.info("user " + invitationChatDto.getUsername() + ", chat id: " + invitationChatDto.getChatId());
            chatService.addUser(invitationChatDto);
            return getOkResponse("User " + invitationChatDto.getUsername() + " has been added to the chat");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/users")
    public ResponseEntity<Object> getUsers(@RequestBody ChatIdDto chatId) {
        try {
            log.info("chat id: " + chatId.getChatId());
            List<UserDto> users = chatService.getChatUsers(chatId);
            return ResponseEntity.ok().body(users);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/invitations")
    public ResponseEntity<Object> getInvitations(@RequestBody UsernameDto usernameDto) {
        try {
            log.info("username: " + usernameDto.getUsername());
            List<InvitationChatDto> invitationsChatsDto = chatService.getInvitations(usernameDto);
            return ResponseEntity.ok().body(invitationsChatsDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/confirm/invitation")
    public ResponseEntity<Object> confirmInvitation(@RequestBody InvitationChatDto invitationChatDto) {
        try {
            log.info("username: " + invitationChatDto.getUsername()
                    + ", chat id: " + invitationChatDto.getChatId()
                    + ", confirmed: " + (invitationChatDto.isConfirmed() ? "CONFIRMED" : "REJECTED"));
            InvitationChatDto invitationChatDtoResp = chatService.confirmInvitation(invitationChatDto);
            return ResponseEntity.ok().body(invitationChatDtoResp);
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
            List<MessageDto<?>> response = messageService.getMessages(chatGetHistoryDto);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<Object> getChats(@RequestBody UsernameDto usernameDto) {
        try {
            log.info("username: " + usernameDto.getUsername());
            List<ChatDto> chats = chatService.getChats(usernameDto.getUsername());
            return ResponseEntity.ok().body(chats);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @DeleteMapping("/delete/chat")
    public ResponseEntity<Object> deleteChat(@RequestBody ChatParticipantDto chatParticipantDto) {
        try {
            log.info("chat id: " + chatParticipantDto.getChatId());
            chatService.deleteChat(chatParticipantDto);
            return getOkResponse("Chat deleted");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PutMapping("/logout")
    public ResponseEntity<Object> logoutOfChat(@RequestBody ChatParticipantDto chatParticipantDto) {
        try {
            log.info("username: " + chatParticipantDto.getUsername() + ", chat id: " + chatParticipantDto.getChatId());
            chatService.logoutOfChat(chatParticipantDto);
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

    @GetMapping("/get/private")
    public ResponseEntity<Object> getPrivateChat(@RequestBody UserFriendDto userFriendDto) {
        try {
            log.info("username: " + userFriendDto.getUsername() + ", friendName: " + userFriendDto.getFriendName());
            ChatDto chatDto = chatService.getPrivateChat(userFriendDto);
            return ResponseEntity.ok().body(chatDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get/personal")
    public ResponseEntity<Object> getPersonalChat(@RequestBody UserDto userDto) {
        try {
            log.info("username: " + userDto.getUsername());
            ChatDto chatDto = chatService.getPersonalChat(userDto);
            return ResponseEntity.ok().body(chatDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
