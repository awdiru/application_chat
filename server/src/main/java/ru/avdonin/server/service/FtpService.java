package ru.avdonin.server.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import ru.avdonin.server.config.FtpConfig;
import ru.avdonin.template.exceptions.FtpClientException;
import ru.avdonin.template.logger.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FtpService {
    private final FtpConfig ftpConfig;
    private final Logger log;
    private final FTPClient ftpClient;

    public FtpService(FtpConfig ftpConfig, Logger log) {
        this.ftpConfig = ftpConfig;
        this.log = log;
        this.ftpClient = new FTPClient();
        try {
            configClient(ftpClient);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void uploadFile(String filename, InputStream inputStream) throws IOException {
        try {
            if (!ftpClient.isConnected())
                throw new FtpClientException("The FTP server is not available");
            if (!ftpClient.storeFile(filename, inputStream))
                throw new FtpClientException("The file was not recorded");

        } catch (FtpClientException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public InputStream getFile(String filename) {
        return null;
    }

    public InputStream getIcon(String filename) {
        /*
        try {
            if (!ftpClient.isConnected())
                throw new FtpClientException("The FTP server is not available");

            InputStream inputStream = ftpClient.retrieveFileStream(filename);
            if (inputStream == null)
                throw new FileNotFoundException("File not found");

            return new FTPInputStreamWrapper(ftpClient, inputStream);

        } catch (FileNotFoundException | FtpClientException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
         */
        return null;
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
