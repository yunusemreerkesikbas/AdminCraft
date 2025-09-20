package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentTranslationJpaRepository extends JpaRepository<ComponentTranslation, Long> {

        @Query("SELECT t FROM ComponentTranslation t WHERE t.component.id = :componentId")
        List<ComponentTranslation> findAllByComponentId(@Param("componentId") Long componentId);

        @Query("SELECT t FROM ComponentTranslation t WHERE t.component.id = :componentId AND t.language = :language")
        Optional<ComponentTranslation> findByComponentIdAndLanguage(@Param("componentId") Long componentId,
                        @Param("language") Language language);

        @Query("SELECT t FROM ComponentTranslation t WHERE t.component.id IN :componentIds AND t.language = :language")
        List<ComponentTranslation> findAllByComponentIdInAndLanguage(@Param("componentIds") List<Long> componentIds,
                        @Param("language") Language language);

        @Query("SELECT t FROM ComponentTranslation t WHERE t.component.id IN :componentIds AND t.language IN :languages")
        List<ComponentTranslation> findAllByComponentIdInAndLanguageIn(@Param("componentIds") List<Long> componentIds,
                        @Param("languages") List<Language> languages);

        @Modifying
        @Query("DELETE FROM ComponentTranslation t WHERE t.component.id = :componentId")
        void deleteByComponentId(@Param("componentId") Long componentId);
}
