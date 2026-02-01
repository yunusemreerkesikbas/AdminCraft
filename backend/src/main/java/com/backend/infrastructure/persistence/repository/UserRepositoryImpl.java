package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;
import com.backend.domain.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final EntityManager entityManager;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public List<User> saveAll(Iterable<User> users) {
        return userJpaRepository.saveAll(users);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return findAll(pageable);
        }

        String searchPattern = "%" + search.toLowerCase().trim() + "%";

        // Build ORDER BY clause from Pageable sort
        StringBuilder orderByClause = new StringBuilder();
        if (pageable.getSort().isSorted()) {
            orderByClause.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                if (orderByClause.length() > 10) { // More than " ORDER BY "
                    orderByClause.append(", ");
                }
                orderByClause.append("u.").append(order.getProperty())
                        .append(" ").append(order.getDirection().name());
            });
        }

        // JPQL query with parameterized search (prevent SQL injection)
        String jpql = """
                SELECT u FROM User u
                WHERE LOWER(u.fullName) LIKE :search
                   OR LOWER(u.email) LIKE :search
                   OR LOWER(u.phone) LIKE :search
                   OR LOWER(u.jobTitle) LIKE :search
                   OR LOWER(u.department) LIKE :search
                """ + orderByClause;

        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("search", searchPattern);

        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<User> users = query.getResultList();

        // Count query
        String countJpql = """
                SELECT COUNT(u) FROM User u
                WHERE LOWER(u.fullName) LIKE :search
                   OR LOWER(u.email) LIKE :search
                   OR LOWER(u.phone) LIKE :search
                   OR LOWER(u.jobTitle) LIKE :search
                   OR LOWER(u.department) LIKE :search
                """;

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("search", searchPattern);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userJpaRepository.findByRole(role);
    }

    @Override
    public List<User> findByRoleIn(List<UserRole> roles) {
        return userJpaRepository.findByRoleIn(roles);
    }

    @Override
    public long countByRole(UserRole role) {
        return userJpaRepository.countByRole(role);
    }

    @Override
    public List<User> findByIsActive(Boolean isActive) {
        return userJpaRepository.findByIsActive(isActive);
    }

    @Override
    public long countByIsActive(Boolean isActive) {
        return userJpaRepository.countByIsActive(isActive);
    }

    @Override
    public List<User> findByEmailVerified(Boolean emailVerified) {
        return userJpaRepository.findByEmailVerified(emailVerified);
    }

    @Override
    public List<User> findByTwoFactorEnabled(Boolean twoFactorEnabled) {
        return userJpaRepository.findByTwoFactorEnabled(twoFactorEnabled);
    }

    @Override
    public List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer attempts) {
        return userJpaRepository.findByFailedLoginAttemptsGreaterThanEqual(attempts);
    }

    @Override
    public List<User> findByLockedUntilAfter(LocalDateTime dateTime) {
        return userJpaRepository.findByLockedUntilAfter(dateTime);
    }

    @Override
    public List<User> findByPasswordChangedAtBefore(LocalDateTime dateTime) {
        return userJpaRepository.findByPasswordChangedAtBefore(dateTime);
    }

    @Override
    public List<User> findByFullNameContainingIgnoreCase(String fullName) {
        return userJpaRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    @Override
    public List<User> findByEmailContainingIgnoreCase(String email) {
        return userJpaRepository.findByEmailContainingIgnoreCase(email);
    }

    @Override
    public List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.findByLastLoginAtBetween(startDate, endDate);
    }

    @Override
    public List<User> findByLastLoginAtAfter(LocalDateTime dateTime) {
        return userJpaRepository.findByLastLoginAtAfter(dateTime);
    }

    @Override
    public List<User> findByLastLoginAtBefore(LocalDateTime dateTime) {
        return userJpaRepository.findByLastLoginAtBefore(dateTime);
    }

    @Override
    public List<User> findByLastLoginAtIsNull() {
        return userJpaRepository.findByLastLoginAtIsNull();
    }

    @Override
    public List<User> findByDepartment(String department) {
        return userJpaRepository.findByDepartment(department);
    }

    @Override
    public List<User> findByJobTitle(String jobTitle) {
        return userJpaRepository.findByJobTitle(jobTitle);
    }

    @Override
    public List<User> findByIdIn(List<Long> ids) {
        return userJpaRepository.findByIdIn(ids);
    }

}