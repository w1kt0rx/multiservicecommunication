package com.example.test.exception;

import org.springframework.http.HttpStatus;

public class RepositoryEntityNotFoundException extends GithubProxyException {
    public RepositoryEntityNotFoundException(String repositoryName) {
        super(String.format("Repository entity %s doesn't exists", repositoryName), HttpStatus.NOT_FOUND);
    }
}
