package com.nice.filehandler.scheduler.FTPtoS3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static boolean moveFileInFTP(FTPClient ftpClient, String sourcePath, String destinationPath, Logger logger) {
        try {
            return ftpClient.rename(sourcePath, destinationPath);
        } catch (IOException e) {
            logger.error("Exception occurred while moving file in FTP", e);
            return false;
        }
    }

    public static void uploadFilesFromFTPToS3(String bucketName, String rootDirectory, String dataDirectory, String processedDirectory, String s3Directory, Integer KEY_TO_PROCESS_BATCH_SIZE, FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // List files in FTP directory under 'rootDirectory/dataDirectory' folder
            String ftpDirectory = rootDirectory + "/" + dataDirectory + "/";
            FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirectory);

            // Process files in batches
            int fileCount = 0;
            for (FTPFile ftpFile : ftpFiles) {
                if (fileCount >= KEY_TO_PROCESS_BATCH_SIZE) break;
                if (!ftpFile.isFile()) continue;

                String fileName = ftpFile.getName();
                String sourcePath = ftpDirectory + fileName;
                String destinationKey = s3Directory + "/" + fileName;

                logger.info("Processing file = {}", sourcePath);

                try (InputStream inputStream = ftpClient.retrieveFileStream(sourcePath)) {
                    if (inputStream != null) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(ftpFile.getSize());
                        amazonS3.putObject(bucketName, destinationKey, inputStream, metadata);
                        ftpClient.completePendingCommand();
                        logger.info("Uploaded to S3: {}", sourcePath);

                        // Move file to 'processedDirectory' folder in FTP
                        String processedPath = rootDirectory + "/" + processedDirectory + "/" + fileName;
                        boolean ftpMoveSuccess = Utils.moveFileInFTP(ftpClient, sourcePath, processedPath, logger);
                        if (ftpMoveSuccess) {
                            logger.info("Moved to processed folder in FTP: {}", processedPath);
                        } else {
                            logger.error("Failed to move to processed folder in FTP: {}", sourcePath);
                        }
                    } else {
                        logger.error("Failed to retrieve file: {}", sourcePath);
                    }
                }
                fileCount++;
            }

        } catch (IOException ex) {
            logger.error("Exception occurred while processing files", ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                logger.error("Exception occurred while disconnecting FTP client", ex);
            }
        }
    }
}
