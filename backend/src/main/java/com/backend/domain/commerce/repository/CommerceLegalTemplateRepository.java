package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;

public interface CommerceLegalTemplateRepository {

	CommerceLegalTemplate save(CommerceLegalTemplate template);

	List<CommerceLegalTemplate> findAll(CommerceLegalTemplateType type, String language, CommerceLegalTemplateStatus status);

	Optional<CommerceLegalTemplate> findByUid(String uid);

	Optional<CommerceLegalTemplate> findByTypeAndLanguageAndStatus(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status);

	List<CommerceLegalTemplate> findByTypeAndLanguageForUpdate(
			CommerceLegalTemplateType type,
			String language);

	boolean acquireTemplateVersionLock(
			CommerceLegalTemplateType type,
			String language);

	void releaseTemplateVersionLock(
			CommerceLegalTemplateType type,
			String language);
}
