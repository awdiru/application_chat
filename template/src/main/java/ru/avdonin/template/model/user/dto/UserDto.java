package ru.avdonin.template.model.user.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.InputStream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDto {
    Long id;
    String username;
    InputStream icon;
}
