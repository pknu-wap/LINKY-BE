//실행 파일

package com.project.linkybe_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

// Linky 백엔드 애플리케이션의 Spring Boot 시작 클래스
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class LinkyBeProjectApplication {

    // Spring Boot 애플리케이션을 실행함.
    public static void main(String[] args) {
        SpringApplication.run(LinkyBeProjectApplication.class, args);
    }
}
