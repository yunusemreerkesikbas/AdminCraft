package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.domain.commerce.repository.CommerceLegalTemplateRepository;
import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceLegalTemplateRepositoryImpl implements CommerceLegalTemplateRepository {

	private final CommerceLegalTemplateJpaRepository jpaRepository;
	private final TenantContextPort tenantContext;

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
	public boolean acquireTemplateVersionLock(
			CommerceLegalTemplateType type,
			String language) {
		return Integer.valueOf(1).equals(jpaRepository.acquireNamedLock(templateVersionLockName(type, language)));
	}

	@Override
	public void releaseTemplateVersionLock(
			CommerceLegalTemplateType type,
			String language) {
		Integer released = jpaRepository.releaseNamedLock(templateVersionLockName(type, language));
		if (!Integer.valueOf(1).equals(released)) {
			throw new IllegalStateException("commerce.legal.template.version.lock.release.failed");
		}
	}

	private String templateVersionLockName(CommerceLegalTemplateType type, String language) {
		String tenantId = tenantContext.getTenantId();
		if (tenantId == null || tenantId.isBlank()) {
			throw new IllegalStateException("commerce.tenant.context.required");
		}
		return "clt_version:" + tenantId.trim() + ":" + type.name() + ":" + language;
	}
}
