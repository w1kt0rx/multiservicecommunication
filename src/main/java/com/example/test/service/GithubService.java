package com.example.test.service;

import com.example.test.client.GithubClient;
import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.exception.RepositoryEntityNotFoundException;
import com.example.test.mapper.RepositoryEntityMapper;
import com.example.test.model.RepositoryEntity;
import com.example.test.repository.RepositoryDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubClient githubClient;
    private final RepositoryEntityMapper repositoryEntityMapper;
    private final RepositoryDetailRepository repositoryDetailRepository;

    public GithubResponse getRepository(String owner, String repositoryName) {
        log.debug("Fetching repository by owner={} and repository name={} from githubClient", owner, repositoryName);
        return githubClient.getRepoByOwnerAndRepoName(owner, repositoryName);
    }

    @Transactional
    public RepositoryResponse saveRepositoryDetails(String owner, String repositoryName) {
        log.debug("Fetching repository details from client by owner={} and repository name={}", owner, repositoryName);
        GithubResponse githubResponse = githubClient.getRepoByOwnerAndRepoName(owner, repositoryName);

        RepositoryEntity entity = repositoryEntityMapper.toEntity(githubResponse);
        RepositoryEntity saved = repositoryDetailRepository.save(entity);

        log.info("Repository information saved successfully, id={}, owner={}, repositoryName={}", saved.getId(), owner, repositoryName);
        return repositoryEntityMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getLocalRepositoryDetails(String owner, String repositoryName) {
        log.debug("Fetching repository by owner={} and repository name={} from local database", owner, repositoryName);
        RepositoryEntity entity = getRepositoryEntity(owner, repositoryName);

        return repositoryEntityMapper.toDto(entity);
    }

    @Transactional
    public RepositoryResponse updateRepositoryDetails(String owner, String repositoryName) {
        log.info("Updating local repository details for owner={} and repositoryName={}", owner, repositoryName);
        RepositoryEntity entity = getRepositoryEntity(owner, repositoryName);

        log.debug("Fetching fresh repository details from GitHub API for owner={} and repositoryName={}", owner, repositoryName);
        GithubResponse githubResponse = githubClient.getRepoByOwnerAndRepoName(owner, repositoryName);

        entity.update(githubResponse);
        RepositoryEntity updated = repositoryDetailRepository.save(entity);

        log.info("Repository information updated successfully, id={}, owner={}, repositoryName={}", updated.getId(), owner, repositoryName);
        return repositoryEntityMapper.toDto(updated);
    }

    @Transactional
    public void deleteRepositoryDetails(String owner, String repositoryName) {
        log.info("Deleting repository details for owner={} and repositoryName={}", owner, repositoryName);
        RepositoryEntity entity = getRepositoryEntity(owner, repositoryName);

        repositoryDetailRepository.delete(entity);
        log.info("Repository successfully deleted from local database, id={}, owner={}, repositoryName={}", entity.getId(), owner, repositoryName);
    }

    private RepositoryEntity getRepositoryEntity(String owner, String repositoryName) {
        return repositoryDetailRepository.findByOwnerAndRepositoryName(owner, repositoryName)
                .orElseThrow(() -> {
                    log.warn("Repository not found in local database for owner={} and repositoryName={}", owner, repositoryName);
                    return new RepositoryEntityNotFoundException(repositoryName);
                });
    }
}