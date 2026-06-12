package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceCustomerRepositoryImpl implements CommerceCustomerRepository {

	private final CommerceCustomerJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceCustomer save(CommerceCustomer customer) {
		return jpaRepository.save(customer);
	}

	@Override
	public Optional<CommerceCustomer> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Optional<CommerceCustomer> findByIdForUpdate(Long id) {
		return jpaRepository.findByIdForUpdate(id);
	}

	@Override
	public Optional<CommerceCustomer> findByUid(String uid) {
		return jpaRepository.findByUid(uid);
	}

	@Override
	public Optional<CommerceCustomer> findByEmailNormalized(String emailNormalized) {
		return jpaRepository.findByEmailNormalized(emailNormalized);
	}

	@Override
	public boolean existsByEmailNormalized(String emailNormalized) {
		return jpaRepository.existsByEmailNormalized(emailNormalized);
	}
}
