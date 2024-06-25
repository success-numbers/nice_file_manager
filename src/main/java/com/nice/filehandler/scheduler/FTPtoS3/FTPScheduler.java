package com.nice.filehandler.scheduler.FTPtoS3;

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
public class FTPScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FTPScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${FTP_ROOT_DIRECTORY}")
    private String ftpRootDirectory;

    @Value("${FTP_DATA_DIRECTORY}")
    private String ftpDataDirectory;

    @Value("${FTP_PROCESSED_DIRECTORY}")
    private String ftpProcessedDirectory;

    @Value("${S3_UPLOAD_DIRECTORY}")
    private String s3UploadDirectory;

    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
    private Integer KEY_TO_PROCESS_BATCH_SIZE;

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void scheduleFileUpload() {
        // Get current system time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("FTPScheduler cron job running at time = {}", formatter.format(currentTime));

        // Call the method to upload files from FTP to S3
        Utils.uploadFilesFromFTPToS3(bucketName, ftpRootDirectory, ftpDataDirectory, ftpProcessedDirectory, s3UploadDirectory, KEY_TO_PROCESS_BATCH_SIZE, ftpConfig, amazonS3, logger);
    }
}
