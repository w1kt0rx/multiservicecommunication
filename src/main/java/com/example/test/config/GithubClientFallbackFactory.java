package com.example.test.config;

import com.example.test.client.GithubClient;
import com.example.test.dto.OwnerDto;
import com.example.test.dto.response.GithubResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubClientFallbackFactory implements FallbackFactory<GithubClient> {
    @Override
    public GithubClient create(Throwable cause) {
        log.error("An exception occurred when calling the the GithubClient", cause);
        return new GithubClient() {
            @Override
            public GithubResponse getRepoByOwnerAndRepoName(String owner, String repo) {
                log.error("Fallback triggered for owner={} and repo={}. Reason: {}", owner, repo, cause.getMessage());
                return new GithubResponse(
                        new OwnerDto(""),
                        repo,
                        owner + "/" + repo,
                        "Fallback response, service unavailable",
                        "",
                        0,
                        ""
                );
            }
        };
    }
}
