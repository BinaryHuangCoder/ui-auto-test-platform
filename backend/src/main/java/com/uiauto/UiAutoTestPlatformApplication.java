package com.uiauto;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.uiauto.mapper")
@EnableScheduling
public class UiAutoTestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiAutoTestPlatformApplication.class, args);
    }
}
