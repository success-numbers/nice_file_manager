package com.nice.filehandler.controller;

import com.nice.filehandler.service.FTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ftp")
public class FTPController {

//    private final FTPService ftpService;
//
//    @Autowired
//    public FTPController(FTPService ftpService) {
//        this.ftpService = ftpService;
//    }
//
//    @GetMapping("/list")
//    public List<String> listFiles() {
//        return ftpService.listFiles();
//    }
//
//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile multipartFile) {
//        String localFilePath = System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename();
//        try {
//            File localFile = new File(localFilePath);
//            multipartFile.transferTo(localFile);
//            ftpService.uploadFile(localFilePath, multipartFile.getOriginalFilename());
//            return "File uploaded successfully.";
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "File upload failed.";
//        }
//    }
//
//    @PostMapping("/cron")
//    public String uploadFile() {
//        ftpService.uploadFilesFromS3ToFTP();
//        return "success";
//    }
}
