package com.nice.filehandler.scheduler;

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
public class OrderExportScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderExportScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${ORDER_EXPORT_ROOT_DIRECTORY}")
    private String orderExportRootDirectory;

    @Value("${ORDER_EXPORT_DATA_DIRECTORY}")
    private String orderExportDataDirectory;

    @Value("${ORDER_EXPORT_PROCESSED_DIRECTORY}")
    private String orderExportProcessedDirectory;

    @Value("${ORDER_EXPORT_FTP_DIRECTORY}")
    private String orderExportFTPDirectory;

    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
    private Integer KEY_TO_PROCESS_BATCH_SIZE;

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void scheduleFileUpload() {
        // Get current system time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Log the cron job running along with the current system time
        logger.info("OrderExportScheduler cron job running at time = {}", formatter.format(currentTime));

        // Call the method to upload files from S3 to FTP
        Utils.uploadFilesFromS3ToFTP(bucketName, orderExportRootDirectory, orderExportDataDirectory, orderExportProcessedDirectory, orderExportFTPDirectory, KEY_TO_PROCESS_BATCH_SIZE, ftpConfig, amazonS3, logger);
    }
}
