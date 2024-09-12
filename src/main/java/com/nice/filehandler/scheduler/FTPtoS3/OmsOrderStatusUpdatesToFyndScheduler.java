package com.nice.filehandler.scheduler.FTPtoS3;

import com.amazonaws.services.s3.AmazonS3;
import com.nice.filehandler.config.FTPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OmsOrderStatusUpdatesToFyndScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OmsOrderStatusUpdatesToFyndScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
    private Integer KEY_TO_PROCESS_BATCH_SIZE;

    @Value("${aws.s3.bucketname}")
    private String s3BucketName;

    @Value("${fromRms.order_status.order_status_updates.S3_PARENT_DIR}")
    private String s3ParentDirectory;

    @Value("${fromRms.order_status.order_status_updates.FTP_PARENT_DIR}")
    private String ftpParentDirectory;

    @Value("${fromRms.order_status.order_status_updates.FTP_DATA_DIR}")
    private String ftpDataDirectory;

    @Value("${fromRms.order_status.order_status_updates.FTP_PROCESSED_DIR}")
    private String ftpProcessedDirectory;

    @Value("${stores.amazon.storeId}")
    private String amazonStoreId;

    @Value("${stores.noon.storeId}")
    private String noonStoreId;


    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void amazonFtpToS3FileUploadScheduler() {
        Utils.schedulerUtil("[AmazonFtpToS3FileUploadScheduler]",
                amazonStoreId,
                s3BucketName,
                s3ParentDirectory,
                ftpParentDirectory, ftpDataDirectory, ftpProcessedDirectory,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void noonFptToS3FileUploadScheduler() {
        Utils.schedulerUtil("[NoonFptToS3FileUploadScheduler]",
                noonStoreId,
                s3BucketName,
                s3ParentDirectory,
                ftpParentDirectory, ftpDataDirectory, ftpProcessedDirectory,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }
}
