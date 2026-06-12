package com.backend.infrastructure.persistence.commerce;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceCustomerConsent;

interface CommerceCustomerConsentJpaRepository extends JpaRepository<CommerceCustomerConsent, Long> {
}
