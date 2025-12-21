package com.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.domain.enums.NavigationItemType;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "navigation_entries", indexes = {
    @Index(columnList = "node_id", name = "idx_entry_node"),
    @Index(columnList = "sort_order", name = "idx_entry_sort")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "node", "i18nContent" })
@NoArgsConstructor
@AllArgsConstructor
public class NavigationEntry extends BaseEntity {

  @NotNull
  @Column(name = "node_id", nullable = false)
  private Long nodeId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "node_id", insertable = false, updatable = false)
  private NavigationNode node;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "item_type", nullable = false, length = 30)
  private NavigationItemType itemType;

  @Size(max = 100)
  @Column(name = "item_id", length = 100)
  private String itemId;

  @Size(max = 500)
  @Column(name = "url", length = 500)
  private String url;

  @Size(max = 10)
  @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Link color must be in HEX format (#RRGGBB)")
  @Column(name = "link_color", length = 10)
  private String linkColor;

  @Size(max = 20)
  @Column(name = "target", length = 20)
  private String target = "_self";

  @Column(name = "is_external")
  private Boolean isExternal = false;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "is_visible")
  private Boolean isVisible = true;

  @ToString.Exclude
  @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<NavigationEntryI18n> i18nContent = new ArrayList<>();

  public boolean isValid() {
    if (itemType == null) {
      return false;
    }
    if (itemType.requiresUrl()) {
      return url != null && !url.isBlank();
    }
    if (itemType.requiresItemId()) {
      return itemId != null && !itemId.isBlank();
    }
    return true;
  }
}
