package com.nice.filehandler.scheduler.S3toFTP;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nice.filehandler.common.UtilsCommon;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Utils {

    public static boolean moveFileInS3(String bucketName, String sourceKey, String destinationKey, AmazonS3 amazonS3, Logger logger) {
        try {
            amazonS3.copyObject(bucketName, sourceKey, bucketName, destinationKey);
            amazonS3.deleteObject(bucketName, sourceKey);
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred while moving file in S3", e);
            return false;
        }
    }

    public static void schedulerUtil(String cronName,
                                     String storeKey,
                                     String s3BucketName,
                                     String s3ParentDirectory, String s3DataDirectory, String s3ProcessedDirectory,
                                     String ftpParentDirectory,
                                     Integer KEY_TO_PROCESS_BATCH_SIZE,
                                     FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
        // Get current system time
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("{} cron job running at time = {}", cronName, formatter.format(currentTime.getTime()));

        String s3RootDir = s3ParentDirectory + "/" + storeKey;
        String ftpRootDir = ftpParentDirectory + "/" + storeKey;

        // Call the method to upload files from S3 to FTP
        uploadFilesFromS3ToFTP(s3BucketName,
                s3RootDir, s3DataDirectory, s3ProcessedDirectory,
                ftpRootDir,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }

    public static void uploadFilesFromS3ToFTP(String s3BucketName,
                                              String s3RootDirectory, String s3DataDirectory, String s3ProcessedDirectory,
                                              String ftpRootDir,
                                              Integer KEY_TO_PROCESS_BATCH_SIZE,
                                              FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
        FTPClient ftpClient = new FTPClient();
        InputStream inputStream = null;
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Ensure the FTP directory exists
            UtilsCommon.createFTPDirectoryIfNotExists(ftpClient, "/" + ftpRootDir, logger);

            // List files in S3 bucket under 'OrderExports/NewOrders' folder
            String s3Directory = s3RootDirectory + "/" + s3DataDirectory + "/";
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(s3BucketName)
                    .withPrefix(s3Directory)
                    .withMaxKeys(KEY_TO_PROCESS_BATCH_SIZE);

            ObjectListing objectListing;
            do {
                objectListing = amazonS3.listObjects(listObjectsRequest);
                List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

                // Filter out folders
                Iterator<S3ObjectSummary> iterator = s3ObjectSummaries.iterator();
                while (iterator.hasNext()) {
                    S3ObjectSummary summary = iterator.next();
                    if (summary.getKey().endsWith("/")) {
                        iterator.remove();
                    }
                }

                logger.info("No of XML files in S3 to process = {}", s3ObjectSummaries.size());

                for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
                    String key = s3ObjectSummary.getKey();
                    String fileName = key.substring(key.lastIndexOf("/") + 1);

                    logger.info("Processing key = {}", key);
                    S3Object s3Object = amazonS3.getObject(s3BucketName, key);
                    try {
                        inputStream = s3Object.getObjectContent();
                        boolean ftpUploadSuccess = ftpClient.storeFile("/" + ftpRootDir + "/" + fileName, inputStream);

                        if (ftpUploadSuccess) {
                            logger.info("Uploaded to FTP: {}", key);

                            // Move file to 'OrderExports/Processed' folder
                            String processedKey = s3RootDirectory + "/" + s3ProcessedDirectory + "/" + fileName;
                            boolean s3MoveSuccess = Utils.moveFileInS3(s3BucketName, key, processedKey, amazonS3, logger);
                            if (s3MoveSuccess) {
                                logger.info("Moved to processed folder in S3: {}", processedKey);
                            } else {
                                logger.error("Failed to move to processed folder in S3: {}", key);
                            }
                        } else {
                            logger.error("Failed to upload: {}", key);
                        }
                    } catch (IOException e) {
                        logger.error("Exception occurred while processing file: {}", key, e);
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                logger.error("Exception occurred while closing input stream", e);
                            }
                        }
                    }
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

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
