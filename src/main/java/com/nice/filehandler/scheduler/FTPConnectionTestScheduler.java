package com.nice.filehandler.scheduler;

import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FTPConnectionTestScheduler {
    private static final Logger logger = LoggerFactory.getLogger(FTPConnectionTestScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Scheduled(fixedRate = 5000)
    public void test() {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // List files in FTP directory under 'rootDirectory/dataDirectory' folder

            // currently we are not using ftpDataDirectory
            // String ftpDirectory = ftpRootDirectory + "/" + ftpDataDirectory + "/";
            String ftpDirectory = "";
            FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirectory);

            logger.info("[TESTING] Total no of files+dirs in FTP = {}", ftpFiles.length);

            // Process files in batches
            int fileCount = 0;
            for (FTPFile ftpFile : ftpFiles) {
                if (fileCount >= 10) break;
                if (!ftpFile.isFile()) {
                    // this check is added so that directories are not included here
                    continue;
                }

                String fileName = ftpFile.getName();

                logger.info("[TESTING] File = {}", fileName);

                fileCount++;
            }
        } catch (IOException ex) {
            logger.error("[TESTING] Exception occurred while processing files", ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                logger.error("[TESTING] Exception occurred while disconnecting FTP client", ex);
            }
        }
    }
}
