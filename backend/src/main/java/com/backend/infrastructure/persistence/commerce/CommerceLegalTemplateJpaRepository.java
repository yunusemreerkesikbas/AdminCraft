package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;

import jakarta.persistence.LockModeType;

interface CommerceLegalTemplateJpaRepository extends JpaRepository<CommerceLegalTemplate, Long> {

	@Query("""
			select template from CommerceLegalTemplate template
			where (:type is null or template.type = :type)
				and (:language is null or template.language = :language)
				and (:status is null or template.status = :status)
			order by template.type asc, template.language asc, template.version desc, template.updatedAt desc
			""")
	List<CommerceLegalTemplate> findAllFiltered(
			@Param("type") CommerceLegalTemplateType type,
			@Param("language") String language,
			@Param("status") CommerceLegalTemplateStatus status);

	Optional<CommerceLegalTemplate> findByUid(String uid);

	Optional<CommerceLegalTemplate> findFirstByTypeAndLanguageAndStatusOrderByVersionDesc(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select template from CommerceLegalTemplate template
			where template.type = :type and template.language = :language
			""")
	List<CommerceLegalTemplate> findByTypeAndLanguageForUpdate(
			@Param("type") CommerceLegalTemplateType type,
			@Param("language") String language);

	@Query(value = "select GET_LOCK(:lockName, 10)", nativeQuery = true)
	Integer acquireNamedLock(@Param("lockName") String lockName);

	@Query(value = "select RELEASE_LOCK(:lockName)", nativeQuery = true)
	Integer releaseNamedLock(@Param("lockName") String lockName);
}
