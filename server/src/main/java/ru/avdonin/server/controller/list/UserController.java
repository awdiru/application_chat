package ru.avdonin.server.controller.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.controller.AbstractController;
import ru.avdonin.server.service.list.UserService;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.user.dto.UserDto;

@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {
    private final UserService userService;

    @Autowired
    public UserController(Logger log, UserService userService) {
        super(log);
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody UserAuthenticationDto userDto) {
        try {
            log.info("registry user: username: " + userDto.getUsername());
            userService.save(userDto);
            return getOkResponse("The user is registered");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserAuthenticationDto userDto) {
        try {
            log.info("login user: " + userDto.getUsername());
            userService.validate(userDto);
            return getOkResponse("The user is logged in");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/avatar/change")
    public ResponseEntity<Object> changeAvatar(@RequestBody UserDto userDto) {
        try {
            log.info("change user avatar: " + userDto.getUsername());
            userService.changeAvatar(userDto);
            return getOkResponse("");
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getUser(@RequestBody UserDto userDto) {
        try {
            UserDto respDto = userService.getUserByUsername(userDto);
            return ResponseEntity.ok().body(respDto);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
