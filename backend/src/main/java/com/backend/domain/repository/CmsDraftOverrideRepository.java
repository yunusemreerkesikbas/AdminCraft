package com.backend.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.CmsDraftOverride;
import com.backend.domain.enums.CmsDraftTargetType;

@Repository
public interface CmsDraftOverrideRepository extends JpaRepository<CmsDraftOverride, Long> {

    Optional<CmsDraftOverride> findByTargetTypeAndTargetIdAndLanguageKey(
        CmsDraftTargetType targetType,
        Long targetId,
        String languageKey);

    List<CmsDraftOverride> findByTargetTypeAndTargetIdIn(
        CmsDraftTargetType targetType,
        Collection<Long> targetIds);

    List<CmsDraftOverride> findByTargetType(CmsDraftTargetType targetType);
}
