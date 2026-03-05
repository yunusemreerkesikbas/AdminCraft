package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformAdminUserRepository extends JpaRepository<PlatformAdminUser, Long> {

  Optional<PlatformAdminUser> findByEmailAndIsActiveTrue(String email);
}
