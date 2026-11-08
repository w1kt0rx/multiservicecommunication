package com.example.test.mapper;

import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.model.RepositoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryEntityMapper {
    RepositoryResponse toDto(RepositoryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "ownerDto.login", target = "owner")
    @Mapping(source = "name", target = "repositoryName")
    @Mapping(source = "full_name", target = "fullName")
    @Mapping(source = "clone_url", target = "cloneUrl")
    @Mapping(source = "stargazers_count", target = "stars")
    @Mapping(source = "created_at", target = "createdAt")
    RepositoryEntity toEntity(GithubResponse response);
}
