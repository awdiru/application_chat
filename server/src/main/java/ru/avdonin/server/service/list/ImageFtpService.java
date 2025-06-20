package ru.avdonin.server.service.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.avdonin.server.service.AbstractFtpService;

@Service
public class ImageFtpService extends AbstractFtpService {
    @Autowired
    public ImageFtpService(@Value("${ftp.host}") String host,
                           @Value("${ftp.port}") Integer port,
                           @Value("${ftp.username}") String username,
                           @Value("${ftp.password}") String password)  {
        super(host, port, username, password,
                "default",
                "default-icon.png",
                "/chats_images",
                100,
                100);
    }
}
