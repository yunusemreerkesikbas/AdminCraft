package com.backend.domain.repository;

import com.backend.domain.entity.PlatformMailOutbox;

public interface PlatformMailOutboxRepository {

    PlatformMailOutbox save(PlatformMailOutbox outbox);
}
