package com.nice.filehandler.scheduler.S3toFTP;

import com.amazonaws.services.s3.AmazonS3;
import com.nice.filehandler.config.FTPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderRTOReturnScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OrderRTOReturnScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
    private Integer KEY_TO_PROCESS_BATCH_SIZE;

    @Value("${aws.s3.bucketname}")
    private String s3BucketName;

    @Value("${toRms.orders.order_rto_returns.S3_PARENT_DIR}")
    private String s3ParentDirectory;

    @Value("${toRms.orders.order_rto_returns.S3_DATA_DIR}")
    private String s3DataDirectory;

    @Value("${toRms.orders.order_rto_returns.S3_PROCESSED_DIR}")
    private String s3ProcessedDirectory;

    @Value("${toRms.orders.order_rto_returns.FTP_PARENT_DIR}")
    private String ftpParentDirectory;

    @Value("${stores.amazon.storeId}")
    private String amazonStoreId;

    @Value("${stores.noon.storeId}")
    private String noonStoreId;

    @Scheduled(cron = "${scheduler.cron-every-10-sec}")
    public void amazonFileUploadScheduler() {
        Utils.schedulerUtil("[AmazonFileUploadScheduler]",
                amazonStoreId,
                s3BucketName,
                s3ParentDirectory, s3DataDirectory, s3ProcessedDirectory,
                ftpParentDirectory,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }

    @Scheduled(cron = "${scheduler.cron-every-10-sec}")
    public void noonFileUploadScheduler() {
        Utils.schedulerUtil("[NoonFileUploadScheduler]",
                noonStoreId,
                s3BucketName,
                s3ParentDirectory, s3DataDirectory, s3ProcessedDirectory,
                ftpParentDirectory,
                KEY_TO_PROCESS_BATCH_SIZE,
                ftpConfig, amazonS3, logger);
    }
}
