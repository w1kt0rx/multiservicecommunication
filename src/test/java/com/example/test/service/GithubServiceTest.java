package com.example.test.service;

import com.example.test.client.GithubClient;
import com.example.test.dto.OwnerDto;
import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.exception.RepositoryEntityNotFoundException;
import com.example.test.mapper.RepositoryEntityMapper;
import com.example.test.model.RepositoryEntity;
import com.example.test.repository.RepositoryDetailRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GithubServiceTest {

    GithubService githubService;
    GithubClient githubClient;
    RepositoryEntityMapper repositoryEntityMapper;
    RepositoryDetailRepository repositoryDetailRepository;

    @BeforeEach
    void setup() {
        this.githubClient = Mockito.mock(GithubClient.class);
        this.repositoryDetailRepository = Mockito.mock(RepositoryDetailRepository.class);
        this.repositoryEntityMapper = Mappers.getMapper(RepositoryEntityMapper.class);
        this.githubService = new GithubService(githubClient, repositoryEntityMapper, repositoryDetailRepository);
    }

    @Test
    void getRepository_dataCorrect_githubResponseReturned() {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        GithubResponse responseFromApi = new GithubResponse(
                new OwnerDto("octocat"),
                "Hello-World",
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(githubClient.getRepoByOwnerAndRepoName(owner, repoName)).thenReturn(responseFromApi);

        //when
        GithubResponse result = githubService.getRepository(owner, repoName);

        //then
        Mockito.verify(githubClient).getRepoByOwnerAndRepoName(ownerCaptor.capture(), repoNameCaptor.capture());
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("Hello-World", result.name()),
                () -> Assertions.assertEquals("octocat/Hello-World", result.full_name()),
                () -> Assertions.assertEquals("My first repo", result.description()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", result.clone_url()),
                () -> Assertions.assertEquals(42, result.stargazers_count()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", result.created_at()),
                () -> Assertions.assertEquals("octocat", result.ownerDto().login()),
                () -> Assertions.assertEquals("octocat", ownerCaptor.getValue()),
                () -> Assertions.assertEquals("Hello-World", repoNameCaptor.getValue())
        );
    }

    @Test
    void saveRepositoryDetails_dataCorrect_repositoryResponseReturnedAndEntitySaved() {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        GithubResponse githubResponse = new GithubResponse(
                new OwnerDto("octocat"),
                "Hello-World",
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );

        RepositoryEntity savedEntity = RepositoryEntity.builder()
                .id(1L)
                .owner(owner)
                .repositoryName(repoName)
                .fullName("octocat/Hello-World")
                .description("My first repo")
                .cloneUrl("https://github.com/octocat/Hello-World.git")
                .stars(42)
                .createdAt("2011-01-26T19:01:12Z")
                .build();

        ArgumentCaptor<RepositoryEntity> entityCaptor = ArgumentCaptor.forClass(RepositoryEntity.class);

        when(githubClient.getRepoByOwnerAndRepoName(owner, repoName)).thenReturn(githubResponse);
        when(repositoryDetailRepository.save(any(RepositoryEntity.class))).thenReturn(savedEntity);

        //when
        RepositoryResponse result = githubService.saveRepositoryDetails(owner, repoName);

        //then
        Mockito.verify(githubClient).getRepoByOwnerAndRepoName(owner, repoName);
        Mockito.verify(repositoryDetailRepository).save(entityCaptor.capture());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("octocat/Hello-World", result.fullName()),
                () -> Assertions.assertEquals("My first repo", result.description()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", result.cloneUrl()),
                () -> Assertions.assertEquals(42, result.stars()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", result.createdAt()),
                () -> Assertions.assertEquals("octocat", entityCaptor.getValue().getOwner()),
                () -> Assertions.assertEquals("Hello-World", entityCaptor.getValue().getRepositoryName()),
                () -> Assertions.assertEquals("octocat/Hello-World", entityCaptor.getValue().getFullName()),
                () -> Assertions.assertEquals("My first repo", entityCaptor.getValue().getDescription()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", entityCaptor.getValue().getCloneUrl()),
                () -> Assertions.assertEquals(42, entityCaptor.getValue().getStars()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", entityCaptor.getValue().getCreatedAt())
        );
    }

    @Test
    void getLocalRepositoryDetails_repositoryExists_repositoryReturned() {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        RepositoryEntity entity = RepositoryEntity.builder()
                .id(1L)
                .owner(owner)
                .repositoryName(repoName)
                .fullName("octocat/Hello-World")
                .description("My first repo")
                .cloneUrl("https://github.com/octocat/Hello-World.git")
                .stars(42)
                .createdAt("2011-01-26T19:01:12Z")
                .build();

        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(repositoryDetailRepository.findByOwnerAndRepositoryName(owner, repoName)).thenReturn(Optional.of(entity));

        //when
        RepositoryResponse result = githubService.getLocalRepositoryDetails(owner, repoName);

        //then
        Mockito.verify(repositoryDetailRepository).findByOwnerAndRepositoryName(ownerCaptor.capture(), repoNameCaptor.capture());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("octocat/Hello-World", result.fullName()),
                () -> Assertions.assertEquals("My first repo", result.description()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", result.cloneUrl()),
                () -> Assertions.assertEquals(42, result.stars()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", result.createdAt()),
                () -> Assertions.assertEquals(owner, ownerCaptor.getValue()),
                () -> Assertions.assertEquals(repoName, repoNameCaptor.getValue())
        );
    }

    @Test
    void getLocalRepositoryDetails_repositoryNotExists_throwsException() {
        //given
        String owner = "octocat";
        String repoName = "Non-Existing-Repo";
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(repositoryDetailRepository.findByOwnerAndRepositoryName(owner, repoName)).thenReturn(Optional.empty());

        //when + then
        RepositoryEntityNotFoundException ex = Assertions.assertThrows(
                RepositoryEntityNotFoundException.class,
                () -> githubService.getLocalRepositoryDetails(owner, repoName)
        );

        Mockito.verify(repositoryDetailRepository).findByOwnerAndRepositoryName(ownerCaptor.capture(), repoNameCaptor.capture());

        Assertions.assertAll(
                () -> Assertions.assertEquals(owner, ownerCaptor.getValue()),
                () -> Assertions.assertEquals(repoName, repoNameCaptor.getValue()),
                () -> Assertions.assertNotNull(ex)
        );
    }
}