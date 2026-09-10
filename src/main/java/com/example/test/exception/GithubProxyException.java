package com.example.test.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GithubProxyException extends RuntimeException {
    private final HttpStatus httpStatus;

    public GithubProxyException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }
}
