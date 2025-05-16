package ru.avdonin.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ftp")
@Getter
@Setter
public class FtpConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String remoteDirectory;
}
