package com.example.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GithubProxy {
    public static void main(String[] args) {
        SpringApplication.run(GithubProxy.class, args);
    }

}
