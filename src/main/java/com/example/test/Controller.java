package com.example.test;

import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/github")
public class Controller {
    private final GithubService service;

    @GetMapping("/owner/{owner}/repository/{repo}")
    public RepositoryResponse get(@PathVariable String owner, @PathVariable String repo) {
        return service.getRepository(owner, repo);
    }
}
