package com.example.test.dto.response;

import com.example.test.dto.OwnerDto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubResponse(
        @JsonProperty("owner")
        OwnerDto ownerDto,
        String name,
        String full_name,
        String description,
        String clone_url,
        Integer stargazers_count,
        String created_at
) {
}
