package com.example.test.exception.handler;

import com.example.test.dto.ErrorMessageDto;
import com.example.test.exception.GithubProxyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GithubProxyException.class)
    ResponseEntity<ErrorMessageDto> handleGithubProxyException(GithubProxyException ex) {
        log.warn("Handled business exception: [{}] Status: {} - Message: {}",
                ex.getClass().getSimpleName(),
                ex.getHttpStatus(),
                ex.getMessage());
        log.debug("Exception stack trace:", ex);

        String message = LocalDateTime.now() + ": " + ex.getMessage();
        return ResponseEntity.status(ex.getHttpStatus()).body(new ErrorMessageDto(message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorMessageDto> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception occurred: [{}] - {}", ex.getClass().getName(), ex.getMessage(), ex);

        String message = LocalDateTime.now() + ": Internal server error";
        return ResponseEntity.internalServerError().body(new ErrorMessageDto(message));
    }
}