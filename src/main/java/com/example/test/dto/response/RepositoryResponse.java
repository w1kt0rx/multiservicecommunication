package com.example.test.dto.response;

public record RepositoryResponse(
        String fullName,
        String description,
        String cloneUrl,
        int stars,
        String createdAt
) {
}