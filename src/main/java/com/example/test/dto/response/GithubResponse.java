package com.example.test.dto.response;

public record GithubResponse(
        String full_name,
        String description,
        String clone_url,
        int stargazers_count,
        String created_at
) {
}
