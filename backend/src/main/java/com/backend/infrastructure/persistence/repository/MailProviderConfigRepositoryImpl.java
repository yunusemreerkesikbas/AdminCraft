package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailProviderConfig;
import com.backend.domain.repository.MailProviderConfigRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MailProviderConfigRepositoryImpl implements MailProviderConfigRepository {

    private final MailProviderConfigJpaRepository jpaRepository;

    @Override
    public Optional<MailProviderConfig> findTopByOrderByIdAsc() {
        return jpaRepository.findTopByOrderByIdAsc();
    }

    @Override
    public MailProviderConfig save(MailProviderConfig config) {
        return jpaRepository.save(config);
    }
}
