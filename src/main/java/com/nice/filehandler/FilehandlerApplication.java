package com.nice.filehandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FilehandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilehandlerApplication.class, args);
	}

}
