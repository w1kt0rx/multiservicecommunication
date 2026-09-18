package com.example.test.client;

import com.example.test.config.GithubClientFallbackFactory;
import com.example.test.config.GithubFeignConfig;
import com.example.test.dto.response.GithubResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "github",
        configuration = GithubFeignConfig.class,
        fallbackFactory = GithubClientFallbackFactory.class)
public interface GithubClient {

    @GetMapping("/repos/{owner}/{repo}")
    GithubResponse getRepoByOwnerAndRepoName(@PathVariable String owner, @PathVariable String repo);
}
