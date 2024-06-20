package com.nice.filehandler.scheduler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Utils {
    public static void createFTPDirectoryIfNotExists(FTPClient ftpClient, String dirPath, Logger logger) throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements.length > 0) {
            StringBuilder path = new StringBuilder();
            for (String singleDir : pathElements) {
                if (!singleDir.isEmpty()) {
                    path.append("/").append(singleDir);
                    if (!ftpClient.changeWorkingDirectory(path.toString())) {
                        if (ftpClient.makeDirectory(path.toString())) {
                            logger.info("Created directory: {}", path.toString());
                        } else {
                            logger.error("Failed to create directory: {}", path.toString());
                        }
                    }
                }
            }
        }
    }

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

    public static void uploadFilesFromS3ToFTP(String bucketName, String rootDirectory, String dataDirectory, String processedDirectory, String ftpDirectory, FTPConfig ftpConfig, AmazonS3 amazonS3, Logger logger) {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Ensure the FTP directory exists
            Utils.createFTPDirectoryIfNotExists(ftpClient, "/" + ftpDirectory, logger);

            // List files in S3 bucket under 'OrderExports/NewOrders' folder
            String s3NewOrdersDirectory = rootDirectory + "/" + dataDirectory + "/";
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(s3NewOrdersDirectory);

            ObjectListing objectListing;
            do {
                objectListing = amazonS3.listObjects(listObjectsRequest);
                List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

                // Filter out folders and non-XML files
                s3ObjectSummaries.removeIf(summary -> summary.getKey().endsWith("/") || !summary.getKey().endsWith(".xml"));
                logger.info("No of XML files in S3 to process = {}", s3ObjectSummaries.size());

                for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
                    String key = s3ObjectSummary.getKey();
                    String fileName = key.substring(key.lastIndexOf("/") + 1);

                    logger.info("Processing key = {}", key);
                    S3Object s3Object = amazonS3.getObject(bucketName, key);
                    try (InputStream inputStream = s3Object.getObjectContent()) {
                        boolean ftpUploadSuccess = ftpClient.storeFile("/" + ftpDirectory + "/" + fileName, inputStream);

                        if (ftpUploadSuccess) {
                            logger.info("Uploaded to FTP: {}", key);

                            // Move file to 'OrderExports/Processed' folder
                            String processedKey = rootDirectory + "/" + processedDirectory + "/" + fileName;
                            boolean s3MoveSuccess = Utils.moveFileInS3(bucketName, key, processedKey, amazonS3, logger);
                            if (s3MoveSuccess) {
                                logger.info("Moved to processed folder in S3: {}", processedKey);
                            } else {
                                logger.error("Failed to move to processed folder in S3: {}", key);
                            }
                        } else {
                            logger.error("Failed to upload: {}", key);
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
