package com.uiauto;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uiauto.mapper")
public class UiAutoTestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiAutoTestPlatformApplication.class, args);
    }
}
