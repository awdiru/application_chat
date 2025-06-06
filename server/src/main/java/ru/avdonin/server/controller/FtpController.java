package ru.avdonin.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.avdonin.server.service.FtpService;
import ru.avdonin.template.exceptions.EmptyFileException;
import ru.avdonin.template.logger.Logger;

import java.io.InputStream;

@RestController
@RequestMapping("/ftp")
public class FtpController extends AbstractController{
    private final FtpService ftpService;

    @Autowired
    public FtpController(Logger log, FtpService ftpService) {
        super(log);
        this.ftpService = ftpService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadIcon(@RequestParam("username") String username,
                                             @RequestBody MultipartFile file) {

        try(InputStream inputStream = file.getInputStream()) {
            log.info("Uploading file: " + file.getOriginalFilename());

            if (file.isEmpty()) throw new EmptyFileException("The file is empty");

            String filename = file.getOriginalFilename();
            if (filename == null) throw new EmptyFileException("The filename is empty");

            ftpService.uploadFile(username + "/" + filename, inputStream);
            return getOkResponse("The file is uploaded");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Object> getIcon(@RequestParam("filename") String filename) {
        try {
            log.info("Downloading file: " + filename);
            InputStream inputStream = ftpService.getIcon(filename);
            String mimeType = determineMimeType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    private String determineMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
