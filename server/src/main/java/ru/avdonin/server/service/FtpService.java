package ru.avdonin.server.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.server.config.FtpConfig;
import ru.avdonin.template.exceptions.FtpClientException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FtpService {
    private final FtpConfig ftpConfig;

    @Autowired
    public FtpService(FtpConfig ftpConfig) {
        this.ftpConfig = ftpConfig;
    }

    public void uploadFile(String filename, InputStream inputStream) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            configClient(ftpClient);
            if (!ftpClient.storeFile(filename, inputStream))
                throw new FtpClientException("The file was not recorded");

        } catch (Exception e) {
            throw new FtpClientException(e.getMessage());

        } finally {
            if (ftpClient.isConnected())
                ftpClient.disconnect();
        }
    }

    private void configClient(FTPClient ftpClient) throws IOException {

        ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        if (!ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDirectory())) {
            ftpClient.makeDirectory(ftpConfig.getRemoteDirectory());
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDirectory());
        }
    }
}
