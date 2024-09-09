package com.nice.filehandler.scheduler.S3toFTP;

import com.amazonaws.services.s3.AmazonS3;
import com.nice.filehandler.config.FTPConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusScheduler.class);

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;
//
//    @Value("${KEY_TO_PROCESS_BATCH_SIZE}")
//    private Integer KEY_TO_PROCESS_BATCH_SIZE;
//
//    @Value("${aws.s3.bucketname}")
//    private String s3BucketName;
//
//    @Value("${toRms.orders.order_status.S3_PARENT_DIR}")
//    private String s3ParentDirectory;
//
//    @Value("${toRms.orders.order_status.S3_DATA_DIR}")
//    private String s3DataDirectory;
//
//    @Value("${toRms.orders.order_status.S3_PROCESSED_DIR}")
//    private String s3ProcessedDirectory;
//
//    @Value("${toRms.orders.order_status.FTP_PARENT_DIR}")
//    private String ftpParentDirectory;
//
//    @Scheduled(fixedRate = 10000) // Run every 10 seconds
//    public void amazonFileUploadScheduler() {
//        Utils.schedulerUtil("[AmazonFileUploadScheduler]",
//                "amazon/",
//                s3BucketName,
//                s3ParentDirectory, s3DataDirectory, s3ProcessedDirectory,
//                ftpParentDirectory,
//                KEY_TO_PROCESS_BATCH_SIZE,
//                ftpConfig, amazonS3, logger);
//    }
//
//    @Scheduled(fixedRate = 10000) // Run every 10 seconds
//    public void noonFileUploadScheduler() {
//        Utils.schedulerUtil("[NoonFileUploadScheduler]",
//                "noon/",
//                s3BucketName,
//                s3ParentDirectory, s3DataDirectory, s3ProcessedDirectory,
//                ftpParentDirectory,
//                KEY_TO_PROCESS_BATCH_SIZE,
//                ftpConfig, amazonS3, logger);
//    }
}
