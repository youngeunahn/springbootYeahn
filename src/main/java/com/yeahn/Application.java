package com.yeahn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Enumeration;
import java.util.Properties;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.default", "local");
        SpringApplication.run(Application.class, args);
 }
}
