package com.nice.filehandler.scheduler.S3toFTP;

import com.amazonaws.services.s3.AmazonS3;
import com.nice.filehandler.config.FTPConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OrderStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${ORDER_STATUS_ROOT_DIRECTORY}")
    private String orderStatusRootDirectory;

    @Value("${ORDER_STATUS_DATA_DIRECTORY}")
    private String orderStatusDataDirectory;

    @Value("${ORDER_STATUS_PROCESSED_DIRECTORY}")
    private String orderStatusProcessedDirectory;

    @Value("${ORDER_STATUS_FTP_DIRECTORY}")
    private String orderStatusFTPDirectory;

    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
    private Integer KEY_TO_PROCESS_BATCH_SIZE;

//    @Scheduled(fixedRate = 20000) // Run every 20 seconds
    public void scheduleFileUpload() {
        // Get current system time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("OrderStatusScheduler cron job running at time = {}", formatter.format(currentTime));

        // Call the method to upload files from S3 to FTP
        Utils.uploadFilesFromS3ToFTP(bucketName, orderStatusRootDirectory, orderStatusDataDirectory, orderStatusProcessedDirectory, orderStatusFTPDirectory, KEY_TO_PROCESS_BATCH_SIZE, ftpConfig, amazonS3, logger);
    }
}
