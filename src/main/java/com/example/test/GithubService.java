package com.example.test;

import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import org.springframework.stereotype.Service;

@Service
public class GithubService {

    private final GithubClient githubClient;
    public GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }
    public RepositoryResponse getRepository(String owner, String repo) {
        GithubResponse githubResponse =
                githubClient.getRepoByOwnerAndRepoName(owner, repo);

        return new RepositoryResponse(
                githubResponse.full_name(),
                githubResponse.description(),
                githubResponse.clone_url(),
                githubResponse.stargazers_count(),
                githubResponse.created_at()
        );
    }
}