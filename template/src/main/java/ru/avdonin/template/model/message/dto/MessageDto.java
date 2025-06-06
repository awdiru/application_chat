package ru.avdonin.template.model.message.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ru.avdonin.template.model.user.dto.UserDto;

import java.io.InputStream;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private OffsetDateTime time;
    @ToString.Exclude
    private String content;
    private String sender;
    private String chat;
    private InputStream file;
    private String locale;
}