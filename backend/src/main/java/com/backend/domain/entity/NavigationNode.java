package com.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.domain.enums.NodePosition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "navigation_nodes", indexes = {
    @Index(columnList = "parent_id", name = "idx_nav_parent"),
    @Index(columnList = "sort_order", name = "idx_nav_sort")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "parent", "children", "entries" })
@NoArgsConstructor
@AllArgsConstructor
public class NavigationNode extends BaseEntity {

  @Size(max = 200)
  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "parent_id")
  private Long parentId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private NavigationNode parent;

  @Enumerated(EnumType.STRING)
  @Column(name = "position", length = 20)
  private NodePosition position = NodePosition.LEFT;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "is_visible")
  private Boolean isVisible = true;

  @Column(name = "is_tab")
  private Boolean isTab = false;

  @ToString.Exclude
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("sortOrder ASC, id ASC")
  private List<NavigationNode> children = new ArrayList<>();

  @ToString.Exclude
  @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("sortOrder ASC, id ASC")
  private List<NavigationEntry> entries = new ArrayList<>();

  public boolean isRoot() {
    return parentId == null;
  }

  public void addChild(NavigationNode child) {
    children.add(child);
    child.setParentId(this.getId());
  }

  public void addEntry(NavigationEntry entry) {
    entries.add(entry);
    entry.setNodeId(this.getId());
  }
}
