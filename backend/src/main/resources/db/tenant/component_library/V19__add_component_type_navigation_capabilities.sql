ALTER TABLE component_types
ADD COLUMN supports_navigation_node BOOLEAN NOT NULL DEFAULT FALSE AFTER category,
ADD COLUMN requires_navigation_node BOOLEAN NOT NULL DEFAULT FALSE AFTER supports_navigation_node,
ADD COLUMN supports_navigation_link_node BOOLEAN NOT NULL DEFAULT FALSE AFTER requires_navigation_node,
ADD COLUMN supports_navigation_type BOOLEAN NOT NULL DEFAULT FALSE AFTER supports_navigation_link_node,
ADD COLUMN supports_search_box BOOLEAN NOT NULL DEFAULT FALSE AFTER supports_navigation_type;
