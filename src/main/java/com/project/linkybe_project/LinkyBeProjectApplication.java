package com.project.linkybe_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LinkyBeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkyBeProjectApplication.class, args);
    }
}
