package com.luidium.cloudsync.repository;

import com.luidium.cloudsync.model.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Long> {
    List<ConnectionEntity> findByIsActive(boolean isActive);
    Optional<ConnectionEntity> findByBucketName(String bucketName);
}
