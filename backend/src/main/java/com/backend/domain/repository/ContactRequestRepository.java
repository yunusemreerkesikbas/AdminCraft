package com.backend.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.ContactRequest;

public interface ContactRequestRepository {

    ContactRequest save(ContactRequest entity);

    Page<ContactRequest> search(String search, String locale, Pageable pageable);
}
