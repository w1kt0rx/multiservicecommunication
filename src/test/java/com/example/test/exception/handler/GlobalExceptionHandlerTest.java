package com.example.test.exception.handler;

import com.example.test.dto.ErrorMessageDto;
import com.example.test.exception.RepositoryEntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleGithubProxyException_repositoryNotFound_returnsNotFoundStatusAndFormattedMessage() {
        // given
        String repoName = "non-existing-repo";
        RepositoryEntityNotFoundException exception = new RepositoryEntityNotFoundException(repoName);

        // when
        ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleGithubProxyException(exception);

        // then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(response),
                () -> Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> Assertions.assertNotNull(response.getBody()),
                () -> {
                    assert response.getBody() != null;
                    Assertions.assertTrue(response.getBody().message().contains("Repository entity non-existing-repo doesn't exists"));
                }
        );
    }

    @Test
    void handleUnexpectedException_genericException_returnsInternalServerError() {
        // given
        Exception exception = new RuntimeException("Unexpected database failure");

        // when
        ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleUnexpectedException(exception);

        // then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(response),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> Assertions.assertNotNull(response.getBody()),
                () -> Assertions.assertTrue(response.getBody().message().contains("Internal server error")),
                () -> Assertions.assertFalse(response.getBody().message().contains("Unexpected database failure")) // upewniamy się, że nie wyciekają szczegóły techniczne
        );
    }
}