package com.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.ContactRequest;
import com.backend.domain.repository.ContactRequestRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContactRequestRepositoryImpl implements ContactRequestRepository {

    private final ContactRequestJpaRepository jpaRepository;

    @Override
    public ContactRequest save(ContactRequest entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Page<ContactRequest> search(String search, Pageable pageable) {
        return jpaRepository.search(search, pageable);
    }
}
