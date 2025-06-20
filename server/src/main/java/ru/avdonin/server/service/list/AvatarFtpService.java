package ru.avdonin.server.service.list;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Service
public class AvatarFtpService {
    public final static String AVATAR_BASE_PATH = "/user_avatars";
    public final static String DEFAULT_AVATAR_PATH = "default";
    public final static String DEFAULT_AVATAR_FILE_NAME = "default-avatar.png";

    private final FTPClient ftp;
    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    @Autowired
    public AvatarFtpService(@Value("${ftp.host}") String host,
                            @Value("${ftp.port}") Integer port,
                            @Value("${ftp.username}") String username,
                            @Value("${ftp.password}") String password) {

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ftp = new FTPClient();

        connectFtp();
        uploadDefaultAvatar();
        disconnectFtp();
    }

    public void createUserDirectory(String username) {
        try {
            connectFtp();
            createDirectoryAndChangeWorking(username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }

    public void uploadAvatar(String username, String avatarFileName, String avatarBase64) {
        try {
            connectFtp();
            ftp.enterLocalPassiveMode();

            byte[] imageData = Base64.getDecoder().decode(avatarBase64);

            createDirectoryAndChangeWorking(username);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                BufferedImage originalImage = ImageIO.read(bais);

                Thumbnails.of(originalImage)
                        .size(100, 100)
                        .antialiasing(Antialiasing.ON)      // Максимальное сглаживание
                        .outputFormat("png")             // Формат PNG для качества
                        .outputQuality(1.0)              // Максимальное качество
                        .keepAspectRatio(false)          // Игнорируем пропорции
                        .toOutputStream(baos);

                byte[] resizedImage = baos.toByteArray();

                createDirectoryAndChangeWorking(username);

                try (InputStream inputStream = new ByteArrayInputStream(resizedImage)) {
                    if (!ftp.storeFile(avatarFileName, inputStream)) {
                        throw new IOException("Upload failed. FTP reply: " + ftp.getReplyString());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }

    public String downloadAvatar(String username, String avatarFileName) {
        try {
            connectFtp();
            ftp.enterLocalPassiveMode();

            String avatarPath = AVATAR_BASE_PATH +
                    (avatarFileName.equals(DEFAULT_AVATAR_FILE_NAME)
                            ? "/" + DEFAULT_AVATAR_PATH + "/" + DEFAULT_AVATAR_FILE_NAME
                            : "/" + username + "/" + avatarFileName);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                if (ftp.retrieveFile(avatarPath, os)) {
                    byte[] avatar = os.toByteArray();
                    return Base64.getEncoder().encodeToString(avatar);
                }
                throw new FileNotFoundException("Avatar not found: " + avatarPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }

    private void uploadDefaultAvatar() {
        Resource defaultAvatar = new ClassPathResource("static/" + DEFAULT_AVATAR_FILE_NAME);
        try (InputStream is = defaultAvatar.getInputStream()) {
            ftp.enterLocalPassiveMode();

            ftp.makeDirectory(AVATAR_BASE_PATH);
            ftp.changeWorkingDirectory(AVATAR_BASE_PATH);

            ftp.makeDirectory(DEFAULT_AVATAR_PATH);
            ftp.changeWorkingDirectory(DEFAULT_AVATAR_PATH);

            ftp.storeFile(DEFAULT_AVATAR_FILE_NAME, is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void connectFtp() {
        try {
            if (!ftp.isConnected()) {
                ftp.connect(this.host, this.port);
                ftp.login(this.username, this.password);
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
            }
        } catch (Exception e) {
            disconnectFtp();
            throw new RuntimeException(e);
        }
    }

    private void disconnectFtp() {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectoryAndChangeWorking(String directoryName) throws IOException {
        String dirPath = AVATAR_BASE_PATH + "/" + directoryName;
        if (!ftp.changeWorkingDirectory(dirPath)) {
            ftp.makeDirectory(dirPath);
            ftp.changeWorkingDirectory(dirPath);
        }
    }
}
