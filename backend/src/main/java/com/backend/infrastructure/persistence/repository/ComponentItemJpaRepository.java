package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentItemJpaRepository extends JpaRepository<ComponentItem, Long> {

  @EntityGraph(attributePaths = { "children" })
  @Query("SELECT i FROM ComponentItem i WHERE i.component.id = :componentId AND i.parent IS NULL ORDER BY i.sortOrder, i.id")
  List<ComponentItem> findRootsByComponentId(@Param("componentId") Long componentId);

  @Query("SELECT i FROM ComponentItem i WHERE i.component.id = :componentId ORDER BY i.parent.id NULLS FIRST, i.level ASC, i.sortOrder ASC, i.id ASC")
  List<ComponentItem> findAllByComponentId(@Param("componentId") Long componentId);

  @Query("SELECT COUNT(i) > 0 FROM ComponentItem i WHERE i.component.id = :componentId AND i.uid = :uid")
  boolean existsByComponentIdAndUid(@Param("componentId") Long componentId, @Param("uid") String uid);

  @Query("SELECT i FROM ComponentItem i WHERE i.id = :id AND i.component.id = :componentId")
  Optional<ComponentItem> findByIdAndComponentId(@Param("id") Long id, @Param("componentId") Long componentId);

  @Query("SELECT i FROM ComponentItem i WHERE i.parent.id = :parentId ORDER BY i.sortOrder, i.id")
  List<ComponentItem> findByParentId(@Param("parentId") Long parentId);
}

