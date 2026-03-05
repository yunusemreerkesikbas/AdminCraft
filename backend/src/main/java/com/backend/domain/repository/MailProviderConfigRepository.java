package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.MailProviderConfig;

public interface MailProviderConfigRepository {

    Optional<MailProviderConfig> findTopByOrderByIdAsc();

    MailProviderConfig save(MailProviderConfig config);
}
