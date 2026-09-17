package com.example.test.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class GithubFeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new GithubErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new GithubRetryer(5,100L);
    }
}
