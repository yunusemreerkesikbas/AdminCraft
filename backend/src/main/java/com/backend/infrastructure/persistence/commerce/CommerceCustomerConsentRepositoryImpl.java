package com.backend.infrastructure.persistence.commerce;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCustomerConsent;
import com.backend.domain.commerce.repository.CommerceCustomerConsentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceCustomerConsentRepositoryImpl implements CommerceCustomerConsentRepository {

	private final CommerceCustomerConsentJpaRepository jpaRepository;

	@Override
	@Transactional
	public List<CommerceCustomerConsent> saveAll(List<CommerceCustomerConsent> consents) {
		return jpaRepository.saveAll(consents);
	}
}
