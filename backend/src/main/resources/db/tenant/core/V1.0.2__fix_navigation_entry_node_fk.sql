ALTER TABLE navigation_entries
    DROP FOREIGN KEY fk_entry_node;

ALTER TABLE navigation_entries
    ADD CONSTRAINT fk_entry_node FOREIGN KEY (node_id) REFERENCES navigation_nodes(id) ON DELETE CASCADE;
