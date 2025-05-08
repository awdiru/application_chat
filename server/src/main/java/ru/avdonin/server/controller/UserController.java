package ru.avdonin.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.service.UserService;
import ru.avdonin.template.exceptions.IncorrectFriendDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.util.ErrorResponse;

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

        } catch (Exception e) {
            return errorHandler(e, "signup");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserAuthenticationDto userDto) {
        try {
            log("login user: " + userDto);
            userService.validate(userDto);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            return errorHandler(e, "login");
        }
    }

    @PostMapping("/friends/add")
    public ResponseEntity<Object> addFriend(@RequestParam String username,
                                            @RequestParam String friendName) {
        try {
            log("addFriend: username: " + username + "; friendName: " + friendName);
            userService.addFriend(username, friendName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            return errorHandler(e, "addFriend");
        }
    }

    @PostMapping("/friends/remove")
    public ResponseEntity<Object> removeFriend(@RequestParam String username,
                                               @RequestParam String friendName) {
        try {
            log("removeFriend: username: " + username + "; friendName: " + friendName);
            userService.removeFriend(username, friendName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            return errorHandler(e, "addFriend");
        }
    }

    @GetMapping("/friends/get")
    public ResponseEntity<Object> getFriends(@RequestParam String username) {
        try {
            log("getFriends: username: " + username);
            List<FriendDto> friends = userService.getFriends(username);
            return ResponseEntity.ok(friends);

        } catch (Exception e) {
            return errorHandler(e, "getFriends");
        }
    }

    @GetMapping("/friends/requests")
    public ResponseEntity<Object> getRequestsFriends(@RequestParam String username) {
        try {
            log("getRequestsFriends: username: " + username);
            List<FriendDto> requestsFriends = userService.getRequestsFriends(username);
            return ResponseEntity.ok(requestsFriends);

        } catch (Exception e) {
            return errorHandler(e, "getFriends");
        }
    }

    @PostMapping("/friends/confirmed")
    public ResponseEntity<Object> confirmedFriend(@RequestParam String username,
                                                  @RequestParam String friendName,
                                                  @RequestParam Boolean confirm) {
        try {
            log("confirmedFriend: username: " + username + "; friendName: " + friendName);
            userService.confirmedFriend(username, friendName, confirm);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            return errorHandler(e, "confirmedFriend");
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

        } catch (Exception e) {
            return errorHandler(e, "renameFriend");
        }
    }

    private ResponseEntity<Object> errorHandler(Exception e, HttpStatus status, String method) {
        log(method + ": ERROR: " + e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), status, e.getMessage()), status);
    }

    private ResponseEntity<Object> errorHandler(Exception e, String method) {
        if (e instanceof IncorrectUserDataException)
            return errorHandler(e, HttpStatus.UNAUTHORIZED, method);
        else if (e instanceof IncorrectFriendDataException)
            return errorHandler(e, HttpStatus.BAD_REQUEST, method);
        else return errorHandler(e, HttpStatus.INTERNAL_SERVER_ERROR, method);
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] UserController: " + text);
    }
}
