package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailProviderConfig;

@Repository
public interface MailProviderConfigJpaRepository extends JpaRepository<MailProviderConfig, Long> {

    Optional<MailProviderConfig> findTopByOrderByIdAsc();
}
