package com.example.test;

import com.example.test.dto.response.GithubResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "github", url = "https://api.github.com/")
public interface GithubClient {

    @GetMapping("/repos/{owner}/{repo}")
    GithubResponse getRepoByOwnerAndRepoName(@PathVariable String owner, @PathVariable String repo);
}
