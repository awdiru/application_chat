package ru.avdonin.server.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.avdonin.server.config.FtpConfig;
import ru.avdonin.template.exceptions.FtpClientException;

import java.io.FileNotFoundException;
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

    public InputStream getIcon(String filename) throws FileNotFoundException {
        FTPClient ftpClient = new FTPClient();
        try {
            configClient(ftpClient);
            InputStream inputStream = ftpClient.retrieveFileStream(filename);
            if (inputStream == null) throw new FileNotFoundException("File not found");
            return new FTPInputStreamWrapper(ftpClient, inputStream);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new FtpClientException(e.getMessage());
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

    private static class FTPInputStreamWrapper extends InputStream {
        private final FTPClient ftpClient;
        private final InputStream wrapped;

        private FTPInputStreamWrapper(FTPClient ftpClient, InputStream wrapped) {
            this.ftpClient = ftpClient;
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            return wrapped.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            wrapped.close();
            if (ftpClient.isConnected()) {
                ftpClient.completePendingCommand();
                ftpClient.disconnect();
            }
        }
    }
}
