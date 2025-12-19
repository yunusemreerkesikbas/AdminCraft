package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NavigationNode;
import com.backend.domain.repository.NavigationNodeRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NavigationNodeRepositoryImpl implements NavigationNodeRepository {

  private final NavigationNodeJpaRepository jpaRepository;

  @Override
  public List<NavigationNode> findRootNodes() {
    return jpaRepository.findByParentIdIsNullOrderBySortOrderAscIdAsc();
  }

  @Override
  public Optional<NavigationNode> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<NavigationNode> findByIdWithChildrenAndEntries(Long id) {
    return jpaRepository.findByIdWithChildrenAndEntries(id);
  }

  @Override
  public Optional<NavigationNode> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<NavigationNode> findByUidWithChildrenAndEntries(String uid) {
    return jpaRepository.findByUidWithChildrenAndEntries(uid);
  }

  @Override
  public List<NavigationNode> findSubtreeByRootId(Long rootId) {
    return jpaRepository.findSubtreeByRootId(rootId);
  }

  @Override
  public List<NavigationNode> findSubtreeByRootUid(String uid) {
    return jpaRepository.findSubtreeByRootUid(uid);
  }

  @Override
  public List<NavigationNode> findByParentId(Long parentId) {
    return jpaRepository.findByParentIdOrderBySortOrderAscIdAsc(parentId);
  }

  @Override
  public NavigationNode save(NavigationNode node) {
    return jpaRepository.save(node);
  }

  @Override
  public List<NavigationNode> saveAll(List<NavigationNode> nodes) {
    return jpaRepository.saveAll(nodes);
  }

  @Override
  public void delete(NavigationNode node) {
    jpaRepository.delete(node);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByUid(String uid) {
    return jpaRepository.existsByUid(uid);
  }

  @Override
  public int calculateDepth(Long nodeId) {
    return jpaRepository.calculateDepth(nodeId);
  }

  @Override
  public boolean isDescendantOf(Long nodeId, Long potentialAncestorId) {
    return jpaRepository.isDescendantOf(nodeId, potentialAncestorId);
  }

  @Override
  public int findMaxChildSortOrderByParentId(Long parentId) {
    Integer result = jpaRepository.findMaxChildSortOrderByParentId(parentId);
    return result != null ? result : -1;
  }
}

interface NavigationNodeJpaRepository extends JpaRepository<NavigationNode, Long> {

  List<NavigationNode> findByParentIdIsNullOrderBySortOrderAscIdAsc();

  List<NavigationNode> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);

  Optional<NavigationNode> findByUid(String uid);

  boolean existsByUid(String uid);

  @EntityGraph(attributePaths = { "children", "entries" })
  @Query("SELECT n FROM NavigationNode n WHERE n.id = :id")
  Optional<NavigationNode> findByIdWithChildrenAndEntries(@Param("id") Long id);

  @EntityGraph(attributePaths = { "children", "entries" })
  @Query("SELECT n FROM NavigationNode n WHERE n.uid = :uid")
  Optional<NavigationNode> findByUidWithChildrenAndEntries(@Param("uid") String uid);

  @Query(value = """
      WITH RECURSIVE subtree AS (
        SELECT * FROM navigation_nodes WHERE id = :rootId
        UNION ALL
        SELECT n.* FROM navigation_nodes n
        INNER JOIN subtree s ON n.parent_id = s.id
      )
      SELECT * FROM subtree
      """, nativeQuery = true)
  List<NavigationNode> findSubtreeByRootId(@Param("rootId") Long rootId);

  @Query(value = """
      WITH RECURSIVE subtree AS (
        SELECT * FROM navigation_nodes WHERE uid = :uid
        UNION ALL
        SELECT n.* FROM navigation_nodes n
        INNER JOIN subtree s ON n.parent_id = s.id
      )
      SELECT * FROM subtree
      """, nativeQuery = true)
  List<NavigationNode> findSubtreeByRootUid(@Param("uid") String uid);

  @Query(value = """
      WITH RECURSIVE node_path AS (
        SELECT id, parent_id, 1 as depth
        FROM navigation_nodes
        WHERE id = :nodeId
        UNION ALL
        SELECT n.id, n.parent_id, np.depth + 1
        FROM navigation_nodes n
        INNER JOIN node_path np ON n.id = np.parent_id
      )
      SELECT COALESCE(MAX(depth), 0) FROM node_path
      """, nativeQuery = true)
  int calculateDepth(@Param("nodeId") Long nodeId);

  @Query(value = """
      WITH RECURSIVE descendants AS (
        SELECT id FROM navigation_nodes WHERE id = :ancestorId
        UNION ALL
        SELECT n.id FROM navigation_nodes n
        INNER JOIN descendants d ON n.parent_id = d.id
      )
      SELECT COUNT(*) > 0 FROM descendants WHERE id = :nodeId AND id != :ancestorId
      """, nativeQuery = true)
  boolean isDescendantOf(@Param("nodeId") Long nodeId, @Param("ancestorId") Long ancestorId);

  @Query("SELECT COALESCE(MAX(n.sortOrder), -1) FROM NavigationNode n WHERE n.parentId = :parentId")
  Integer findMaxChildSortOrderByParentId(@Param("parentId") Long parentId);
}
