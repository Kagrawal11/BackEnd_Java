package com.example.backend_java;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.*")
@EnableJpaRepositories(basePackages = "com.example.*")
@EntityScan(basePackages = "com.example.*")
@EnableAsync
public class BackEndJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackEndJavaApplication.class, args);
    }
}
