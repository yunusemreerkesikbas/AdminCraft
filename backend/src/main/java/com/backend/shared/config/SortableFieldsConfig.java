package com.backend.shared.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.backend.presentation.dto.response.SortOptionDto;

/**
 * Configuration for sortable fields per entity.
 * Defines allowed sort fields and their i18n labels.
 */
public final class SortableFieldsConfig {

        private SortableFieldsConfig() {
                // Utility class
        }

        // ========== Media Entity ==========
        public static final Set<String> MEDIA_ALLOWED_FIELDS = Set.of(
                        "createdAt", "originalName", "fileSize", "fileType", "mimeType");

        public static final String MEDIA_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> MEDIA_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("originalName,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("originalName,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("fileSize,desc", "admin.sort.sizeDesc"),
                        SortOptionDto.of("fileSize,asc", "admin.sort.sizeAsc"));

        // ========== Navigation Node Entity ==========
        public static final Set<String> NAVIGATION_NODE_ALLOWED_FIELDS = Set.of(
                        "createdAt", "uid", "sortOrder", "title");

        public static final String NAVIGATION_NODE_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> NAVIGATION_NODE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("uid,asc", "admin.sort.uidAsc"),
                        SortOptionDto.of("uid,desc", "admin.sort.uidDesc"),
                        SortOptionDto.of("sortOrder,asc", "admin.sort.orderAsc"),
                        SortOptionDto.of("sortOrder,desc", "admin.sort.orderDesc"),
                        SortOptionDto.of("title,asc", "admin.sort.titleAsc"),
                        SortOptionDto.of("title,desc", "admin.sort.titleDesc"));

        // ========== Page Template Entity ==========
        public static final Set<String> PAGE_TEMPLATE_ALLOWED_FIELDS = Set.of(
                        "id", "createdAt", "name", "uid");

        public static final String PAGE_TEMPLATE_DEFAULT_SORT = "id,desc";

        public static final List<SortOptionDto> PAGE_TEMPLATE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("id,desc", "admin.sort.newest"),
                        SortOptionDto.of("id,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("uid,asc", "admin.sort.uidAsc"),
                        SortOptionDto.of("uid,desc", "admin.sort.uidDesc"),
                        SortOptionDto.of("createdAt,desc", "admin.sort.createdDesc"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.createdAsc"));

        // ========== Component Entity ==========
        public static final Set<String> COMPONENT_ALLOWED_FIELDS = Set.of(
                        "createdAt", "name", "uid", "displayOrder");

        public static final String COMPONENT_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> COMPONENT_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("displayOrder,asc", "admin.sort.orderAsc"),
                        SortOptionDto.of("displayOrder,desc", "admin.sort.orderDesc"));

        // ========== ComponentType Entity ==========
        public static final Set<String> COMPONENT_TYPE_ALLOWED_FIELDS = Set.of(
                        "createdAt", "name", "uid", "category");

        public static final String COMPONENT_TYPE_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> COMPONENT_TYPE_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("name,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("name,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("category,asc", "admin.sort.categoryAsc"),
                        SortOptionDto.of("category,desc", "admin.sort.categoryDesc"));

        // ========== User Entity ==========
        public static final Set<String> USER_ALLOWED_FIELDS = Set.of(
                        "createdAt", "email", "role", "isActive", "lastLoginAt");

        public static final String USER_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> USER_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("email,asc", "admin.sort.emailAsc"),
                        SortOptionDto.of("email,desc", "admin.sort.emailDesc"),
                        SortOptionDto.of("role,asc", "admin.sort.roleAsc"),
                        SortOptionDto.of("role,desc", "admin.sort.roleDesc"),
                        SortOptionDto.of("lastLoginAt,desc", "admin.sort.lastLoginDesc"),
                        SortOptionDto.of("lastLoginAt,asc", "admin.sort.lastLoginAsc"));

        // ========== Mail Subscriber Entity ==========
        public static final Set<String> MAIL_SUBSCRIBER_ALLOWED_FIELDS = Set.of(
                        "createdAt", "email", "status", "confirmedAt", "unsubscribedAt");

        public static final String MAIL_SUBSCRIBER_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> MAIL_SUBSCRIBER_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("email,asc", "admin.sort.emailAsc"),
                        SortOptionDto.of("email,desc", "admin.sort.emailDesc"),
                        SortOptionDto.of("status,asc", "admin.sort.statusAsc"),
                        SortOptionDto.of("status,desc", "admin.sort.statusDesc"),
                        SortOptionDto.of("confirmedAt,asc", "admin.sort.confirmedAtAsc"),
                        SortOptionDto.of("confirmedAt,desc", "admin.sort.confirmedAtDesc"),
                        SortOptionDto.of("unsubscribedAt,asc", "admin.sort.unsubscribedAtAsc"),
                        SortOptionDto.of("unsubscribedAt,desc", "admin.sort.unsubscribedAtDesc"));

        public static final Set<String> DEMO_REQUEST_ALLOWED_FIELDS = Set.of(
                        "createdAt", "fullName", "email", "locale", "source");

        public static final String DEMO_REQUEST_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> DEMO_REQUEST_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("fullName,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("fullName,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("email,asc", "admin.sort.emailAsc"),
                        SortOptionDto.of("email,desc", "admin.sort.emailDesc"));

        public static final Set<String> CONTACT_REQUEST_ALLOWED_FIELDS = Set.of(
                        "createdAt", "fullName", "subject", "locale", "source");

        public static final String CONTACT_REQUEST_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> CONTACT_REQUEST_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("fullName,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("fullName,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("subject,asc", "admin.sort.titleAsc"),
                        SortOptionDto.of("subject,desc", "admin.sort.titleDesc"));

        public static final Set<String> SITE_ACTIVITY_ALLOWED_FIELDS = Set.of(
                        "createdAt");

        public static final String SITE_ACTIVITY_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> SITE_ACTIVITY_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"));

        public static final Set<String> SITE_ACTIVITY_TREND_ALLOWED_FIELDS = Set.of(
                        "date");

        public static final String SITE_ACTIVITY_TREND_DEFAULT_SORT = "date,desc";

        public static final List<SortOptionDto> SITE_ACTIVITY_TREND_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("date,desc", "admin.sort.newest"),
                        SortOptionDto.of("date,asc", "admin.sort.oldest"));

	// ========== Commerce Order Entity ==========
	public static final Set<String> COMMERCE_ORDER_ALLOWED_FIELDS = Set.of(
			"createdAt", "total", "orderNumber", "status");

	public static final String COMMERCE_ORDER_DEFAULT_SORT = "createdAt,desc";

	public static final List<SortOptionDto> COMMERCE_ORDER_SORT_OPTIONS = List.of(
			SortOptionDto.defaultOption("createdAt,desc", "commerce.order.sort.newest"),
			SortOptionDto.of("createdAt,asc", "commerce.order.sort.oldest"),
			SortOptionDto.of("total,desc", "commerce.order.sort.totalDesc"),
			SortOptionDto.of("total,asc", "commerce.order.sort.totalAsc"),
			SortOptionDto.of("orderNumber,asc", "commerce.order.sort.orderNumberAsc"),
			SortOptionDto.of("orderNumber,desc", "commerce.order.sort.orderNumberDesc"),
			SortOptionDto.of("status,asc", "commerce.order.sort.statusAsc"),
			SortOptionDto.of("status,desc", "commerce.order.sort.statusDesc"));

	public static final Set<String> COMMERCE_ADMIN_ORDER_ALLOWED_FIELDS = Set.of(
			"createdAt", "total", "orderNumber", "status", "requiresAttention");

	public static final String COMMERCE_ADMIN_ORDER_DEFAULT_SORT = "createdAt,desc";

	public static final List<SortOptionDto> COMMERCE_ADMIN_ORDER_SORT_OPTIONS = List.of(
			SortOptionDto.defaultOption("createdAt,desc", "admin.commerce.sort.newest"),
			SortOptionDto.of("createdAt,asc", "admin.commerce.sort.oldest"),
			SortOptionDto.of("total,desc", "admin.commerce.sort.totalDesc"),
			SortOptionDto.of("total,asc", "admin.commerce.sort.totalAsc"),
			SortOptionDto.of("orderNumber,asc", "admin.commerce.sort.orderNumberAsc"),
			SortOptionDto.of("orderNumber,desc", "admin.commerce.sort.orderNumberDesc"),
			SortOptionDto.of("status,asc", "admin.commerce.sort.statusAsc"),
			SortOptionDto.of("status,desc", "admin.commerce.sort.statusDesc"),
			SortOptionDto.of("requiresAttention,desc", "admin.commerce.sort.attentionDesc"));

	public static final Set<String> COMMERCE_ADMIN_PAYMENT_ATTEMPT_ALLOWED_FIELDS = Set.of(
			"createdAt", "expiresAt", "total", "status", "provider");

	public static final String COMMERCE_ADMIN_PAYMENT_ATTEMPT_DEFAULT_SORT = "createdAt,desc";

	public static final List<SortOptionDto> COMMERCE_ADMIN_PAYMENT_ATTEMPT_SORT_OPTIONS = List.of(
			SortOptionDto.defaultOption("createdAt,desc", "admin.commerce.sort.newest"),
			SortOptionDto.of("createdAt,asc", "admin.commerce.sort.oldest"),
			SortOptionDto.of("expiresAt,asc", "admin.commerce.sort.expiresAsc"),
			SortOptionDto.of("expiresAt,desc", "admin.commerce.sort.expiresDesc"),
			SortOptionDto.of("total,desc", "admin.commerce.sort.totalDesc"),
			SortOptionDto.of("total,asc", "admin.commerce.sort.totalAsc"),
			SortOptionDto.of("status,asc", "admin.commerce.sort.statusAsc"),
			SortOptionDto.of("status,desc", "admin.commerce.sort.statusDesc"),
			SortOptionDto.of("provider,asc", "admin.commerce.sort.providerAsc"));

	public static final Set<String> COMMERCE_ADMIN_ORDER_REQUEST_ALLOWED_FIELDS = Set.of(
			"createdAt", "status", "type", "refundStatus");

	public static final String COMMERCE_ADMIN_ORDER_REQUEST_DEFAULT_SORT = "createdAt,desc";

	public static final List<SortOptionDto> COMMERCE_ADMIN_ORDER_REQUEST_SORT_OPTIONS = List.of(
			SortOptionDto.defaultOption("createdAt,desc", "admin.commerce.sort.newest"),
			SortOptionDto.of("createdAt,asc", "admin.commerce.sort.oldest"),
			SortOptionDto.of("status,asc", "admin.commerce.sort.statusAsc"),
			SortOptionDto.of("status,desc", "admin.commerce.sort.statusDesc"),
			SortOptionDto.of("type,asc", "admin.commerce.sort.typeAsc"),
			SortOptionDto.of("refundStatus,asc", "admin.commerce.sort.refundStatusAsc"));

	public static final Set<String> COMMERCE_NOTIFICATION_OUTBOX_ALLOWED_FIELDS = Set.of(
			"createdAt", "status", "eventType", "lastAttemptedAt", "nextRetryAt");

	public static final String COMMERCE_NOTIFICATION_OUTBOX_DEFAULT_SORT = "createdAt,desc";

	public static final List<SortOptionDto> COMMERCE_NOTIFICATION_OUTBOX_SORT_OPTIONS = List.of(
			SortOptionDto.defaultOption("createdAt,desc", "admin.commerce.sort.newest"),
			SortOptionDto.of("createdAt,asc", "admin.commerce.sort.oldest"),
			SortOptionDto.of("status,asc", "admin.commerce.sort.statusAsc"),
			SortOptionDto.of("status,desc", "admin.commerce.sort.statusDesc"),
			SortOptionDto.of("eventType,asc", "admin.commerce.sort.eventTypeAsc"),
			SortOptionDto.of("eventType,desc", "admin.commerce.sort.eventTypeDesc"),
			SortOptionDto.of("lastAttemptedAt,desc", "admin.commerce.sort.lastAttemptedDesc"),
			SortOptionDto.of("nextRetryAt,asc", "admin.commerce.sort.nextRetryAsc"));

        // ========== Tenant Entity ==========
        public static final Set<String> TENANT_ALLOWED_FIELDS = Set.of(
                        "createdAt", "companyName", "subdomain", "status");

        public static final String TENANT_DEFAULT_SORT = "createdAt,desc";

        public static final List<SortOptionDto> TENANT_SORT_OPTIONS = List.of(
                        SortOptionDto.defaultOption("createdAt,desc", "admin.sort.newest"),
                        SortOptionDto.of("createdAt,asc", "admin.sort.oldest"),
                        SortOptionDto.of("companyName,asc", "admin.sort.nameAsc"),
                        SortOptionDto.of("companyName,desc", "admin.sort.nameDesc"),
                        SortOptionDto.of("subdomain,asc", "admin.sort.subdomainAsc"),
                        SortOptionDto.of("subdomain,desc", "admin.sort.subdomainDesc"),
                        SortOptionDto.of("status,asc", "admin.sort.statusAsc"),
                        SortOptionDto.of("status,desc", "admin.sort.statusDesc"));

        /**
         * Registry mapping entity names to their allowed fields.
         */
        public static final Map<String, Set<String>> ENTITY_FIELDS = Map.ofEntries(
                        Map.entry("Media", MEDIA_ALLOWED_FIELDS),
                        Map.entry("NavigationNode", NAVIGATION_NODE_ALLOWED_FIELDS),
                        Map.entry("PageTemplate", PAGE_TEMPLATE_ALLOWED_FIELDS),
                        Map.entry("Component", COMPONENT_ALLOWED_FIELDS),
                        Map.entry("ComponentType", COMPONENT_TYPE_ALLOWED_FIELDS),
                        Map.entry("User", USER_ALLOWED_FIELDS),
                        Map.entry("MailSubscriber", MAIL_SUBSCRIBER_ALLOWED_FIELDS),
                        Map.entry("DemoRequest", DEMO_REQUEST_ALLOWED_FIELDS),
                        Map.entry("ContactRequest", CONTACT_REQUEST_ALLOWED_FIELDS),
                        Map.entry("Tenant", TENANT_ALLOWED_FIELDS));

        public static final Map<String, List<SortOptionDto>> ENTITY_SORT_OPTIONS = Map.ofEntries(
                        Map.entry("Media", MEDIA_SORT_OPTIONS),
                        Map.entry("NavigationNode", NAVIGATION_NODE_SORT_OPTIONS),
                        Map.entry("PageTemplate", PAGE_TEMPLATE_SORT_OPTIONS),
                        Map.entry("Component", COMPONENT_SORT_OPTIONS),
                        Map.entry("ComponentType", COMPONENT_TYPE_SORT_OPTIONS),
                        Map.entry("User", USER_SORT_OPTIONS),
                        Map.entry("MailSubscriber", MAIL_SUBSCRIBER_SORT_OPTIONS),
                        Map.entry("DemoRequest", DEMO_REQUEST_SORT_OPTIONS),
                        Map.entry("ContactRequest", CONTACT_REQUEST_SORT_OPTIONS),
                        Map.entry("Tenant", TENANT_SORT_OPTIONS));

        public static final Map<String, String> ENTITY_DEFAULT_SORT = Map.ofEntries(
                        Map.entry("Media", MEDIA_DEFAULT_SORT),
                        Map.entry("NavigationNode", NAVIGATION_NODE_DEFAULT_SORT),
                        Map.entry("PageTemplate", PAGE_TEMPLATE_DEFAULT_SORT),
                        Map.entry("Component", COMPONENT_DEFAULT_SORT),
                        Map.entry("ComponentType", COMPONENT_TYPE_DEFAULT_SORT),
                        Map.entry("User", USER_DEFAULT_SORT),
                        Map.entry("MailSubscriber", MAIL_SUBSCRIBER_DEFAULT_SORT),
                        Map.entry("DemoRequest", DEMO_REQUEST_DEFAULT_SORT),
                        Map.entry("ContactRequest", CONTACT_REQUEST_DEFAULT_SORT),
                        Map.entry("Tenant", TENANT_DEFAULT_SORT));
}
