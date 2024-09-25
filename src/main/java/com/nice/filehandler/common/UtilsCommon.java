package com.nice.filehandler.common;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;

import java.io.IOException;

public class UtilsCommon {
    public static void createFTPDirectoryIfNotExists(FTPClient ftpClient, String dirPath, Logger logger) throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements.length > 0) {
            StringBuilder path = new StringBuilder();
            for (String singleDir : pathElements) {
                if (!singleDir.isEmpty()) {
                    path.append("/").append(singleDir);
                    if (!ftpClient.changeWorkingDirectory(path.toString())) {
                        if (ftpClient.makeDirectory(path.toString())) {
                            logger.info("Created directory: {}", path.toString());
                        } else {
                            logger.error("Failed to create directory: {}", path.toString());
                        }
                    }
                }
            }
        }
    }
}