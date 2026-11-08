package com.example.test.mapper;

import com.example.test.dto.OwnerDto;
import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.model.RepositoryEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class RepositoryEntityMapperTest {

    private final RepositoryEntityMapper mapper = Mappers.getMapper(RepositoryEntityMapper.class);

    @Test
    void toEntity_dataCorrect_returnsRepositoryEntity() {
        //given
        OwnerDto ownerDto = new OwnerDto("octocat");
        GithubResponse githubResponse = new GithubResponse(
                ownerDto,
                "Hello-World",
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );

        //when
        RepositoryEntity entity = mapper.toEntity(githubResponse);

        //then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(entity),
                () -> Assertions.assertNull(entity.getId()),
                () -> Assertions.assertEquals("octocat", entity.getOwner()),
                () -> Assertions.assertEquals("Hello-World", entity.getRepositoryName()),
                () -> Assertions.assertEquals("octocat/Hello-World", entity.getFullName()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", entity.getCloneUrl()),
                () -> Assertions.assertEquals(42, entity.getStars()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", entity.getCreatedAt()),
                () -> Assertions.assertEquals("My first repo", entity.getDescription())
        );
    }

    @Test
    void toDto_dataCorrect_returnsRepositoryResponse() {
        //given
        RepositoryEntity entity = RepositoryEntity.builder()
                .id(1L)
                .owner("octocat")
                .repositoryName("Hello-World")
                .fullName("octocat/Hello-World")
                .description("My first repo")
                .cloneUrl("https://github.com/octocat/Hello-World.git")
                .stars(42)
                .createdAt("2011-01-26T19:01:12Z")
                .build();

        //when
        RepositoryResponse response = mapper.toDto(entity);

        //then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(response),
                () -> Assertions.assertEquals(1L, response.id()),
                () -> Assertions.assertEquals("octocat", response.owner()),
                () -> Assertions.assertEquals("Hello-World", response.repositoryName()),
                () -> Assertions.assertEquals("octocat/Hello-World", response.fullName()),
                () -> Assertions.assertEquals("My first repo", response.description()),
                () -> Assertions.assertEquals("https://github.com/octocat/Hello-World.git", response.cloneUrl()),
                () -> Assertions.assertEquals(42, response.stars()),
                () -> Assertions.assertEquals("2011-01-26T19:01:12Z", response.createdAt())
        );
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        //when
        RepositoryEntity entity = mapper.toEntity(null);

        //then
        Assertions.assertNull(entity);
    }

    @Test
    void toDto_nullInput_returnsNull() {
        //when
        RepositoryResponse response = mapper.toDto(null);

        //then
        Assertions.assertNull(response);
    }
}