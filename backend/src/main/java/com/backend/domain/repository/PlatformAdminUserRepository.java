package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.PlatformAdminUser;

public interface PlatformAdminUserRepository {

    Optional<PlatformAdminUser> findByEmailAndIsActiveTrue(String email);

    PlatformAdminUser save(PlatformAdminUser admin);
}
