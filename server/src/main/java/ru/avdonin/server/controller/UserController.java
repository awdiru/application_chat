package ru.avdonin.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.template.exceptions.IncorrectFriendDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.util.ErrorResponse;
import ru.avdonin.server.service.UserService;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody UserAuthenticationDto userDto) {
        try {
            log("registry user: " + userDto);
            userService.save(userDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IncorrectUserDataException e) {
            return errorHandler(e, HttpStatus.UNAUTHORIZED, "signup");
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "signup");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserAuthenticationDto userDto) {
        try {
            log("login user: " + userDto);
            userService.validate(userDto);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IncorrectUserDataException e) {
            return errorHandler(e, HttpStatus.UNAUTHORIZED, "login");
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "login");
        }
    }

    @PostMapping("/friends/add")
    public ResponseEntity<Object> addFriend(@RequestParam String username,
                                            @RequestParam String friendName) {
        try {
            log("addFriend: username: " + username + "; friendName: " + friendName);
            userService.addFriend(username, friendName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IncorrectUserDataException e) {
            return errorHandler(e, HttpStatus.UNAUTHORIZED, "addFriend");
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "addFriend");
        }
    }

    @GetMapping("/friends/get")
    public ResponseEntity<Object> getFriends(@RequestParam String username) {
        try {
            log("getFriends: username: " + username);
            List<FriendDto> friends = userService.getFriends(username);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "getFriends");
        }
    }

    @GetMapping("/friends/requests")
    public ResponseEntity<Object> getRequestsFriends(@RequestParam String username) {
        try {
            log("getRequestsFriends: username: " + username);
            List<FriendDto> requestsFriends = userService.getRequestsFriends(username);
            return ResponseEntity.ok(requestsFriends);
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "getFriends");
        }
    }

    @PatchMapping("/friends/confirmed/{confirm}")
    public ResponseEntity<Object> confirmedFriend(@RequestParam String username,
                                                  @RequestParam String friendName,
                                                  @PathVariable Boolean confirm) {
        try {
            log("confirmedFriend: username: " + username + "; friendName: " + friendName);
            userService.confirmedFriend(username, friendName, confirm);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IncorrectUserDataException e) {
            return errorHandler(e, HttpStatus.UNAUTHORIZED, "confirmedFriend");
        } catch (IncorrectFriendDataException e) {
            return errorHandler(e, HttpStatus.BAD_REQUEST, "confirmedFriend");
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "confirmedFriend");
        }
    }

    @PatchMapping("/friend/rename")
    public ResponseEntity<Object> renameFriend(@RequestParam String username,
                                               @RequestParam String friendName,
                                               @RequestParam String newFriendName) {
        try {
            log("renameFriend: username: " + username + "; friendName: " + friendName + "; newFriendName: " + newFriendName);
            userService.renameFriend(username, friendName, newFriendName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IncorrectUserDataException e) {
            return errorHandler(e, HttpStatus.UNAUTHORIZED, "renameFriend");
        } catch (IncorrectFriendDataException e) {
            return errorHandler(e, HttpStatus.BAD_REQUEST, "renameFriend");
        } catch (Exception e) {
            return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, "renameFriend");
        }
    }

    private ResponseEntity<Object> errorHandler(Exception e, HttpStatus status, String method) {
        log(method + ": ERROR: " + e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), status, e.getMessage()), status);
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] UserController: " + text);
    }
}
