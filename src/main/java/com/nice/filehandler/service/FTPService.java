package com.nice.filehandler.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nice.filehandler.config.FTPConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FTPService {

    @Autowired
    private FTPConfig ftpConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    public void uploadFile(String localFilePath, String remoteFileName) {
        FTPClient ftpClient = new FTPClient();
        try (InputStream inputStream = new FileInputStream(localFilePath)) {
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean done = ftpClient.storeFile(remoteFileName, inputStream);
            if (done) {
                System.out.println("File is uploaded successfully.");
            } else {
                System.out.println("Failed to upload the file.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> listFiles() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles();
            return Arrays.stream(files)
                    .map(FTPFile::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadFilesFromS3ToFTP() {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect to FTP server
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // List files in S3 bucket
            ObjectListing objectListing = amazonS3.listObjects(bucketName);
            List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

            for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
                String key = s3ObjectSummary.getKey();
                S3Object s3Object = amazonS3.getObject(bucketName, key);
                try (InputStream inputStream = s3Object.getObjectContent()) {
                    boolean done = ftpClient.storeFile(key, inputStream);
                    if (done) {
                        System.out.println("Uploaded: " + key);
                        // Move file to "processed" folder
                        String processedKey = "processed/" + key;
                        amazonS3.copyObject(bucketName, key, bucketName, processedKey);
                        amazonS3.deleteObject(bucketName, key);
                    } else {
                        System.out.println("Failed to upload: " + key);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
