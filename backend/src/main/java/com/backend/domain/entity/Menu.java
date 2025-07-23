package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"tenant", "site", "parent", "children"})
@ToString(exclude = {"tenant", "site", "parent", "children"})
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Menü adı boş olamaz")
    @Size(max = 100, message = "Menü adı 100 karakterden uzun olamaz")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 200, message = "Menü başlığı 200 karakterden uzun olamaz")
    @Column(name = "title", length = 200)
    private String title;

    @NotNull(message = "Dil belirtilmelidir")
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language = Language.TR;

    @NotNull(message = "Tenant ID boş olamaz")
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "site_id")
    private Long siteId;

    // Menu item properties
    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "css_class", length = 100)
    private String cssClass;

    @Column(name = "target", length = 20)
    private String target = "_self"; // _self, _blank, _parent, _top

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_visible", nullable = false)
    private Boolean visible = true;

    // SEO properties
    @Size(max = 160, message = "Meta açıklama 160 karakterden uzun olamaz")
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Size(max = 200, message = "Anahtar kelimeler 200 karakterden uzun olamaz")
    @Column(name = "meta_keywords", length = 200)
    private String metaKeywords;

    // Hierarchy properties
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "path", length = 500)
    private String path; // For hierarchical path like "1/2/3"

    // Access control
    @Column(name = "requires_auth", nullable = false)
    private Boolean requiresAuth = false;

    @Column(name = "allowed_roles", length = 200)
    private String allowedRoles; // Comma-separated role names

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Menu parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Menu> children = new ArrayList<>();

    // Business Methods
    public boolean isRootMenu() {
        return parentId == null || parentId == 0;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void addChild(Menu child) {
        if (child != null) {
            child.setParentId(this.id);
            child.setLevel(this.level + 1);
            child.updatePath();
            this.children.add(child);
        }
    }

    public void removeChild(Menu child) {
        if (child != null) {
            child.setParentId(null);
            child.setLevel(0);
            child.setPath(null);
            this.children.remove(child);
        }
    }

    public void updatePath() {
        if (isRootMenu()) {
            this.path = String.valueOf(this.id);
        } else if (parent != null) {
            this.path = parent.getPath() + "/" + this.id;
        }
    }

    public boolean isAccessibleByRole(String userRole) {
        if (!requiresAuth) {
            return true;
        }
        
        if (allowedRoles == null || allowedRoles.trim().isEmpty()) {
            return true;
        }

        String[] roles = allowedRoles.split(",");
        for (String role : roles) {
            if (role.trim().equalsIgnoreCase(userRole)) {
                return true;
            }
        }
        return false;
    }

    public boolean isExternalUrl() {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public boolean canBeDeleted() {
        return !hasChildren();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
        // Also deactivate children
        if (hasChildren()) {
            children.forEach(Menu::deactivate);
        }
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
        // Also hide children
        if (hasChildren()) {
            children.forEach(Menu::hide);
        }
    }

    public String getDisplayUrl() {
        if (url == null || url.trim().isEmpty()) {
            return "#";
        }
        return url;
    }

    public String getDisplayTitle() {
        return title != null && !title.trim().isEmpty() ? title : name;
    }

    // Constructor
    public Menu() {
        // Default constructor
    }

    public Menu(String name, String url, Language language, Long tenantId) {
        this.name = name;
        this.url = url;
        this.language = language;
        this.tenantId = tenantId;
    }
}