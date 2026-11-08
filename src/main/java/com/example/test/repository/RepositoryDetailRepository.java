package com.example.test.repository;

import com.example.test.model.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositoryDetailRepository extends JpaRepository<RepositoryEntity, Long> {
    Optional<RepositoryEntity> findByOwnerAndRepositoryName(String owner, String repositoryName);
}
