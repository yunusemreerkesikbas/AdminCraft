package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformDemoRequest;
import com.backend.domain.repository.PlatformDemoRequestRepository;
import com.backend.domain.util.UuidUidGenerator;
import com.backend.infrastructure.persistence.platform.mapper.PlatformDemoRequestMapper;
import com.backend.infrastructure.persistence.platform.repository.PlatformDemoRequestJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Component
public class PlatformDemoRequestPersistenceAdapter implements PlatformDemoRequestRepository {

    private final PlatformDemoRequestJpaRepository jpaRepository;
    private final PlatformDemoRequestMapper mapper;

    @PersistenceContext(unitName = "platform")
    private EntityManager entityManager;

    public PlatformDemoRequestPersistenceAdapter(
            PlatformDemoRequestJpaRepository jpaRepository,
            PlatformDemoRequestMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PlatformDemoRequest save(PlatformDemoRequest entity) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(entity)));
    }

    @Override
    public Page<PlatformDemoRequest> search(String search, Pageable pageable) {
        return jpaRepository.search(search, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<PlatformDemoRequest> saveIfNotDuplicateWithinWindow(PlatformDemoRequest domain,
            LocalDateTime windowStart) {
        String emailNorm = domain.getEmail() == null ? ""
                : domain.getEmail().trim().toLowerCase(Locale.ROOT);
        String uuid = UuidUidGenerator.generateUuid();
        String uid = UuidUidGenerator.generateUid();
        LocalDateTime now = LocalDateTime.now();

        Query q = entityManager.createNativeQuery("""
                INSERT INTO platform_management.platform_demo_requests
                (uuid, uid, full_name, email, phone, message, locale, source, client_ip, user_agent, created_at, updated_at, created_by, updated_by)
                SELECT :uuid, :uid, :fullName, :email, :phone, :message, :locale, :source, :clientIp, :userAgent, :now, :now, NULL, NULL
                FROM DUAL
                WHERE NOT EXISTS (
                  SELECT 1 FROM platform_management.platform_demo_requests p
                  WHERE LOWER(p.email) = LOWER(:emailExists)
                    AND (p.client_ip <=> :clientIpExists)
                    AND p.created_at >= :since
                )
                """);
        q.setParameter("uuid", uuid);
        q.setParameter("uid", uid);
        q.setParameter("fullName", domain.getFullName());
        q.setParameter("email", emailNorm);
        q.setParameter("phone", domain.getPhone());
        q.setParameter("message", domain.getMessage());
        q.setParameter("locale", domain.getLocale());
        q.setParameter("source", domain.getSource() != null ? domain.getSource() : "landing");
        q.setParameter("clientIp", domain.getClientIp());
        q.setParameter("userAgent", domain.getUserAgent());
        q.setParameter("now", now);
        q.setParameter("emailExists", emailNorm);
        q.setParameter("clientIpExists", domain.getClientIp());
        q.setParameter("since", windowStart);

        int inserted = q.executeUpdate();
        if (inserted == 0) {
            return Optional.empty();
        }
        entityManager.flush();
        return jpaRepository.findByUuid(uuid).map(mapper::toDomain);
    }
}
