package com.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ContactRequest;
import com.backend.domain.repository.ContactRequestRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContactRequestRepositoryImpl implements ContactRequestRepository {

    private final ContactRequestJpaRepository jpaRepository;

    @Override
    @Transactional("tenantTransactionManager")
    public ContactRequest save(ContactRequest entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Page<ContactRequest> search(String search, String locale, Pageable pageable) {
        return jpaRepository.search(search, locale, pageable);
    }
}
