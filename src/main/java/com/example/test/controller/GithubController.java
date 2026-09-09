package com.example.test.controller;

import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GithubController {
    private final GithubService service;

    @GetMapping("/owner/{owner}/repository/{repositoryName}")
    public GithubResponse get(@PathVariable String owner, @PathVariable String repositoryName) {
        return service.getRepository(owner, repositoryName);
    }

    @PostMapping("/repositories/{owner}/{repositoryName}")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse saveRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        return service.saveRepositoryDetails(owner, repositoryName);
    }

    @GetMapping("/local/repositories/{owner}/{repositoryName}")
    public RepositoryResponse getLocalRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        return service.getLocalRepositoryDetails(owner, repositoryName);
    }
}
