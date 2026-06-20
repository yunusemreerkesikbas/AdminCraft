package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.domain.commerce.repository.CommerceLegalTemplateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceLegalTemplateRepositoryImpl implements CommerceLegalTemplateRepository {

	private final CommerceLegalTemplateJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceLegalTemplate save(CommerceLegalTemplate template) {
		return jpaRepository.save(template);
	}

	@Override
	public List<CommerceLegalTemplate> findAll(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status) {
		return jpaRepository.findAllFiltered(type, language, status);
	}

	@Override
	public Optional<CommerceLegalTemplate> findByUid(String uid) {
		return jpaRepository.findByUid(uid);
	}

	@Override
	public Optional<CommerceLegalTemplate> findByTypeAndLanguageAndStatus(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status) {
		return jpaRepository.findFirstByTypeAndLanguageAndStatusOrderByVersionDesc(type, language, status);
	}

	@Override
	public List<CommerceLegalTemplate> findByTypeAndLanguageForUpdate(
			CommerceLegalTemplateType type,
			String language) {
		return jpaRepository.findByTypeAndLanguageForUpdate(type, language);
	}

	@Override
	public int nextVersion(CommerceLegalTemplateType type, String language) {
		return jpaRepository.maxVersion(type, language) + 1;
	}
}
