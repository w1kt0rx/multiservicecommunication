package com.example.test.controller;

import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GithubController {

    private final GithubService service;

    @GetMapping("/owner/{owner}/repository/{repositoryName}")
    public GithubResponse get(@PathVariable String owner, @PathVariable String repositoryName) {
        log.debug("Received GET request for external GitHub repository: owner={}, repositoryName={}", owner, repositoryName);
        return service.getRepository(owner, repositoryName);
    }

    @PostMapping("/repositories/{owner}/{repositoryName}")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse saveRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        log.info("Received POST request to save repository: owner={}, repositoryName={}", owner, repositoryName);
        return service.saveRepositoryDetails(owner, repositoryName);
    }

    @GetMapping("/local/repositories/{owner}/{repositoryName}")
    public RepositoryResponse getLocalRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        log.debug("Received GET request for local repository: owner={}, repositoryName={}", owner, repositoryName);
        return service.getLocalRepositoryDetails(owner, repositoryName);
    }

    @PutMapping("/repositories/{owner}/{repositoryName}")
    public RepositoryResponse updateRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        log.info("Received PUT request to update repository: owner={}, repositoryName={}", owner, repositoryName);
        return service.updateRepositoryDetails(owner, repositoryName);
    }

    @DeleteMapping("/repositories/{owner}/{repositoryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRepository(@PathVariable String owner, @PathVariable String repositoryName) {
        log.info("Received DELETE request for repository: owner={}, repositoryName={}", owner, repositoryName);
        service.deleteRepositoryDetails(owner, repositoryName);
    }
}