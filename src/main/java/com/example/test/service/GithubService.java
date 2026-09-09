package com.example.test.service;

import com.example.test.client.GithubClient;
import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.exception.RepositoryEntityNotFoundException;
import com.example.test.mapper.RepositoryEntityMapper;
import com.example.test.model.RepositoryEntity;
import com.example.test.repository.RepositoryDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubClient githubClient;
    private final RepositoryEntityMapper repositoryEntityMapper;
    private final RepositoryDetailRepository repositoryDetailRepository;

    public GithubResponse getRepository(String owner, String repositoryName) {
        return githubClient.getRepoByOwnerAndRepoName(owner, repositoryName);
    }

    public RepositoryResponse saveRepositoryDetails(String owner, String repositoryName) {
        GithubResponse githubResponse = githubClient.getRepoByOwnerAndRepoName(owner, repositoryName);
        RepositoryEntity entity = repositoryEntityMapper.toEntity(githubResponse);

        return repositoryEntityMapper.toDto(repositoryDetailRepository.save(entity));
    }

    public RepositoryResponse getLocalRepositoryDetails(String owner, String repositoryName) {
        RepositoryEntity entity = repositoryDetailRepository.findByOwnerAndRepositoryName(owner, repositoryName)
                .orElseThrow(() -> new RepositoryEntityNotFoundException(repositoryName));

        return repositoryEntityMapper.toDto(entity);
    }
}