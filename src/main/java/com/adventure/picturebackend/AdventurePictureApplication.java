package com.adventure.picturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("com.adventure.picturebackend.mapper")
@SpringBootApplication
@EnableAsync
public class AdventurePictureApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdventurePictureApplication.class, args);
    }
}
