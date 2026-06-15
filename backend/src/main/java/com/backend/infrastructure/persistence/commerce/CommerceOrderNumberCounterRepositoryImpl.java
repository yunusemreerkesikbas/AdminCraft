package com.backend.infrastructure.persistence.commerce;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.repository.CommerceOrderNumberCounterRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceOrderNumberCounterRepositoryImpl implements CommerceOrderNumberCounterRepository {

	private final EntityManager entityManager;

	@Override
	@Transactional
	public int nextSequence(String prefix, LocalDate orderDate) {
		entityManager.createNativeQuery("""
				INSERT INTO commerce_order_number_counters (
					uuid,
					uid,
					prefix,
					order_date,
					last_sequence,
					created_at,
					updated_at
				)
				VALUES (
					UUID(),
					CONCAT('ordcnt_', ?, '_', DATE_FORMAT(?, '%Y%m%d')),
					?,
					?,
					LAST_INSERT_ID(1),
					NOW(),
					NOW()
				)
				ON DUPLICATE KEY UPDATE
					last_sequence = LAST_INSERT_ID(last_sequence + 1),
					updated_at = NOW()
				""")
				.setParameter(1, prefix)
				.setParameter(2, Date.valueOf(orderDate))
				.setParameter(3, prefix)
				.setParameter(4, Date.valueOf(orderDate))
				.executeUpdate();
		Number sequence = (Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()")
				.getSingleResult();
		return sequence.intValue();
	}
}
