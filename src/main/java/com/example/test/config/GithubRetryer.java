package com.example.test.config;

import feign.RetryableException;
import feign.Retryer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class GithubRetryer implements Retryer {
    private final int retryMaxAttempt;
    private final long retryInterval;
    private int attempt = 1;

    public GithubRetryer(int retryMaxAttempt, Long retryInterval) {
        this.retryMaxAttempt = retryMaxAttempt;
        this.retryInterval = retryInterval;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Feign retry attempt {} due to {}", attempt, e.getMessage());

        if (attempt >= retryMaxAttempt) {
            throw e;
        }
        attempt++;
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new GithubRetryer(this.retryMaxAttempt, this.retryInterval);
    }
}
