package ru.avdonin.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.avdonin.server.service.FtpService;
import ru.avdonin.template.exceptions.EmptyFileException;

import java.io.InputStream;

import static ru.avdonin.template.model.util.ResponseBuilder.getErrorResponse;
import static ru.avdonin.template.model.util.ResponseBuilder.getOkResponse;

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
}
