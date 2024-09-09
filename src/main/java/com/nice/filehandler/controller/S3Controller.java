package com.nice.filehandler.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nice.filehandler.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/s3")
public class S3Controller {

//    private final S3Service s3Service;
//
//    @Autowired
//    public S3Controller(S3Service s3Service) {
//        this.s3Service = s3Service;
//    }
//
//    @GetMapping("/")
//    public List<Bucket> getAllBuckets() {
//        return s3Service.getAllBuckets();
//    }
//
//    @GetMapping("/{bucketName}")
//    public List<S3ObjectSummary> getBucketFiles(@PathVariable String bucketName) {
//        return s3Service.listFiles(bucketName);
//    }
//
//    @GetMapping("/{bucketName}/{keyName}")
//    public ResponseEntity<byte[]> getFile(@PathVariable String bucketName, @PathVariable String keyName) throws IOException {
//        InputStream inputStream = s3Service.getFile(bucketName, keyName);
//        byte[] content = inputStream.readAllBytes();
//        inputStream.close();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + keyName + "\"");
//
//        return new ResponseEntity<>(content, headers, HttpStatus.OK);
//    }
}
