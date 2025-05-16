package ru.avdonin.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.avdonin.server.service.FtpService;
import ru.avdonin.template.exceptions.EmptyFileException;
import ru.avdonin.template.exceptions.FtpClientException;
import ru.avdonin.template.model.util.ResponseMessage;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/ftp")
public class FileUploadController {
    private final FtpService ftpService;

    @Autowired
    public FileUploadController(FtpService ftpService) {
        this.ftpService = ftpService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("username") String username,
                                             @RequestBody MultipartFile file) {

        try(InputStream inputStream = file.getInputStream()) {
            if (file.isEmpty()) throw new EmptyFileException("The file is empty");

            String filename = file.getOriginalFilename();
            ftpService.uploadFile(filename, inputStream);
            return getOkResponse("The file is uploaded");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    private ResponseEntity<Object> getOkResponse(String message) {
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .message(message)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok().body(responseMessage);
    }

    private ResponseEntity<Object> getErrorResponse(Exception e) {
        HttpStatus status = getErrorStatus(e);
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .status(status)
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(responseMessage);
    }

    private HttpStatus getErrorStatus(Exception e) {
        if (e instanceof EmptyFileException) return HttpStatus.BAD_REQUEST;
        else if (e instanceof FtpClientException) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
