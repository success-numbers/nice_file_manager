package com.nice.filehandler.service;

import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;

import java.io.InputStream;
import java.util.List;


@Service
public class S3Service {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketname}")
    private String bucketName;


    @Autowired
    public S3Service() {

    }

//    public List<Bucket> getAllBuckets() {
//        return amazonS3.listBuckets();
//    }
//
//    public List<S3ObjectSummary> listFiles(String bucketName) {
//        ObjectListing objectListing = amazonS3.listObjects(bucketName);
//        return objectListing.getObjectSummaries();
//    }
//
//    public InputStream getFile(String bucketName, String keyName) {
//        S3Object s3Object = amazonS3.getObject(bucketName, keyName);
//        return s3Object.getObjectContent();
//    }
}
