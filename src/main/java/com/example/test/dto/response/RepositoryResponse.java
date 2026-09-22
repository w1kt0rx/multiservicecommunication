package com.example.test.dto.response;

public record RepositoryResponse(
        Long id,
        String owner,
        String repositoryName,
        String fullName,
        String description,
        String cloneUrl,
        int stars,
        String createdAt
) {
}