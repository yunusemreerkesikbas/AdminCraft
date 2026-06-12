package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceCustomerAddress;
import com.backend.domain.commerce.repository.CommerceCustomerAddressRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceCustomerAddressRepositoryImpl implements CommerceCustomerAddressRepository {

	private final CommerceCustomerAddressJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceCustomerAddress save(CommerceCustomerAddress address) {
		return jpaRepository.save(address);
	}

	@Override
	public List<CommerceCustomerAddress> findByCustomerId(Long customerId) {
		return jpaRepository.findByCustomerIdOrderByIdAsc(customerId);
	}

	@Override
	public Optional<CommerceCustomerAddress> findByCustomerIdAndUid(Long customerId, String uid) {
		return jpaRepository.findByCustomerIdAndUid(customerId, uid);
	}

	@Override
	@Transactional
	public void delete(CommerceCustomerAddress address) {
		jpaRepository.delete(address);
	}

	@Override
	@Transactional
	public int clearDefaultDelivery(Long customerId) {
		return jpaRepository.clearDefaultDelivery(customerId);
	}

	@Override
	@Transactional
	public int clearDefaultBilling(Long customerId) {
		return jpaRepository.clearDefaultBilling(customerId);
	}
}
