package com.nice.filehandler.scheduler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class S3ToFTPScheduler {

    private static final Logger logger = LoggerFactory.getLogger(S3ToFTPScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${ORDER_EXPORT_DIRECTORY}")
    private String orderExportDirectory;

    @Value("${ORDER_EXPORT_PROCESSED_DIRECTORY}")
    private String orderExportProcessedDirectory;

    @Value("${ORDER_EXPORT_FTP_DIRECTORY}")
    private String orderExportFTPDirectory;

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void scheduleFileUpload() {
        // Get current system time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("Cron job running at time = {}", formatter.format(currentTime));

        // Call the method to upload files from S3 to FTP
        uploadFilesFromS3ToFTP();
    }


    public void uploadFilesFromS3ToFTP() {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // List files in S3 bucket excluding "processed" folder
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withDelimiter("/" + orderExportDirectory)
                    .withPrefix(""); // Start from the root

            ObjectListing objectListing;
            do {
                objectListing = amazonS3.listObjects(listObjectsRequest);
                List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

                logger.info("No of files in S3 to process = {}", s3ObjectSummaries.size());

                for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
                    String key = s3ObjectSummary.getKey();
                    String fileName = key.substring(key.lastIndexOf("/") + 1);

                    // Skip files in the "processed" folder
                    if (key.startsWith(orderExportProcessedDirectory)) {
                        logger.info("Skipping file in processed folder: {}", key);
                        continue;
                    }

                    logger.info("Processing key = {}", key);
                    S3Object s3Object = amazonS3.getObject(bucketName, key);
                    try (InputStream inputStream = s3Object.getObjectContent()) {
                        boolean ftpUploadSuccess = ftpClient.storeFile(orderExportFTPDirectory, inputStream);
                        if (ftpUploadSuccess) {
                            logger.info("Uploaded to FTP: {}", key);
                            // Move file to "processed" folder
                            String processedKey = orderExportProcessedDirectory + '/' + fileName;
                            boolean s3MoveSuccess = moveFileInS3(bucketName, key, processedKey);
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

    private boolean moveFileInS3(String bucketName, String sourceKey, String destinationKey) {
        try {
            amazonS3.copyObject(bucketName, sourceKey, bucketName, destinationKey);
            amazonS3.deleteObject(bucketName, destinationKey);
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred while moving file in S3", e);
            return false;
        }
    }
}
