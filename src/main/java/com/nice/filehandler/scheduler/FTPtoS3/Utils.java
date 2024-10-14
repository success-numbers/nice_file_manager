package com.nice.filehandler.scheduler.FTPtoS3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.nice.filehandler.common.UtilsCommon;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static boolean moveFileInFTP(FTPClient ftpClient, String sourcePath, String destinationPath, Logger logger) {
        try {
            return ftpClient.rename("/" + sourcePath, "/" + destinationPath);
        } catch (IOException e) {
            logger.error("Exception occurred while moving file in FTP", e);
            return false;
        }
    }

    public static void schedulerUtil(String cronName,
                                     String storeKey,
                                     String s3BucketName,
                                     String s3ParentDirectory,
                                     String ftpParentDirectory, String ftpDataDirectory, String ftpProcessedDirectory,
                                     Integer KEY_TO_PROCESS_BATCH_SIZE,
                                     FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
        // Get current system time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("{} cron job running at time = {}", cronName, formatter.format(currentTime));

        String s3RootDir = s3ParentDirectory + "/" + storeKey;
//        String ftpRootDir = ftpParentDirectory + "/" + storeKey;
        String ftpRootDir = ftpParentDirectory.replace("<STORE_ID>", storeKey);

        // Call the method to upload files from S3 to FTP
        uploadFilesFromFTPToS3(s3BucketName,
                ftpRootDir,
                ftpDataDirectory,
                ftpProcessedDirectory,
                s3RootDir,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }

    public static void uploadFilesFromFTPToS3(String bucketName, String ftpRootDirectory, String ftpDataDirectory, String ftpProcessedDirectory, String s3Directory, Integer KEY_TO_PROCESS_BATCH_SIZE, FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
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
            String ftpDirectory = ftpRootDirectory + "/";
            FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirectory);

            logger.info("No of XML files (+directories which will be ignored) in FTP to process = {}", ftpFiles.length);

            // Process files in batches
            int fileCount = 0;
            for (FTPFile ftpFile : ftpFiles) {
                if (fileCount >= KEY_TO_PROCESS_BATCH_SIZE) break;
                if (!ftpFile.isFile()) {
                    // this check is added so that directories are not included here
                    continue;
                }

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

                        // Ensure the FTP processed directory exists
                        String processedDir = ftpRootDirectory + "/" + ftpProcessedDirectory;
                        UtilsCommon.createFTPDirectoryIfNotExists(ftpClient, "/" + processedDir, logger);

                        // Move file to 'processedDirectory' folder in FTP
                        String processedPath = ftpRootDirectory + "/" + ftpProcessedDirectory + "/" + fileName;
                        boolean ftpMoveSuccess = Utils.moveFileInFTP(ftpClient, sourcePath, processedPath, logger);
                        if (ftpMoveSuccess) {
                            logger.info("Moved to processed folder in FTP: {}", processedPath);
                        } else {
                            logger.error("Failed to move to processed folder from FTP: {} to FTP: {}", sourcePath, processedPath);
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
