package ru.avdonin.server.service;

import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public abstract class AbstractFtpService {

    /**
     * Название директории по умолчанию.
     * Необходимо переопределять!
     */
    protected final String defaultPath;
    /**
     * Название файла по умолчанию.
     * Необходимо переопределять!
     */
    @Getter
    protected final String defaultFileName;
    /**
     * Название базовой директории.
     * Необходимо переопределять!
     */
    protected final String basePath;
    /**
     * Сжатие по X.
     * Необходимо переопределять!
     */
    protected final Integer xCompression;
    /**
     * Сжатие по Y.
     * Необходимо переопределять!
     */
    protected final Integer yCompression;

    protected final FTPClient ftp;
    protected final String host;
    protected final Integer port;
    protected final String username;
    protected final String password;

    public AbstractFtpService(String host,
                              Integer port,
                              String username,
                              String password,
                              String defaultPath,
                              String defaultFileName,
                              String basePath,
                              Integer xCompression,
                              Integer yCompression) {

        this.defaultPath = defaultPath;
        this.defaultFileName = defaultFileName;
        this.ftp = new FTPClient();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.basePath = basePath;
        this.xCompression = xCompression;
        this.yCompression = yCompression;

        connectFtp();
        uploadDefault();
        disconnectFtp();
    }

    public void createDirectory(String directoryName) {
        try {
            connectFtp();
            createDirectoryAndChangeWorking(directoryName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }

    public void upload(String directoryName, String fileName, String fileBase64) {
        try {
            connectFtp();
            ftp.enterLocalPassiveMode();

            byte[] imageData = Base64.getDecoder().decode(fileBase64);

        //    createDirectoryAndChangeWorking(directoryName);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                BufferedImage originalImage = ImageIO.read(bais);

                Thumbnails.of(originalImage)
                        .size(xCompression, yCompression)   // Размер сжатия
                        .antialiasing(Antialiasing.ON)      // Максимальное сглаживание
                        .outputFormat("png")             // Формат PNG для качества
                        .outputQuality(1.0)              // Максимальное качество
                        .keepAspectRatio(false)          // Игнорируем пропорции
                        .toOutputStream(baos);

                byte[] resizedImage = baos.toByteArray();

                createDirectoryAndChangeWorking(directoryName);

                try (InputStream inputStream = new ByteArrayInputStream(resizedImage)) {
                    if (!ftp.storeFile(fileName, inputStream)) {
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

    public String download(String directoryName, String fileName) {
        try {
            connectFtp();
            ftp.enterLocalPassiveMode();

            String path = basePath + (fileName.equals(defaultFileName)
                    ? "/" + defaultPath + "/" + defaultFileName
                    : "/" + directoryName + "/" + fileName);

            String fileBase64 = download(path);
            if (fileBase64 != null) return fileBase64;

            path = basePath + "/" + defaultPath + "/" + defaultFileName;

            fileBase64 = download(path);
            if (fileBase64 != null) return fileBase64;

            throw new FileNotFoundException("File not found: " + path);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }

    private String download(String path) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (ftp.retrieveFile(path, os)) {
                byte[] avatar = os.toByteArray();
                return Base64.getEncoder().encodeToString(avatar);
            }
        }
        return null;
    }

    private void createDirectoryAndChangeWorking(String directoryName) throws IOException {
        String dirPath = basePath + "/" + directoryName;
        if (!ftp.changeWorkingDirectory(dirPath)) {
            ftp.makeDirectory(dirPath);
            ftp.changeWorkingDirectory(dirPath);
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

    private void uploadDefault() {
        Resource defaultAvatar = new ClassPathResource("static/" + defaultFileName);
        try (InputStream is = defaultAvatar.getInputStream()) {
            connectFtp();
            ftp.enterLocalPassiveMode();

            ftp.makeDirectory(basePath);
            ftp.changeWorkingDirectory(basePath);

            ftp.makeDirectory(defaultPath);
            ftp.changeWorkingDirectory(defaultPath);

            ftp.storeFile(defaultFileName, is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFtp();
        }
    }
}
