-- Subject is free-form text; leading-wildcard admin search cannot use this B-tree index effectively.
ALTER TABLE contact_requests DROP INDEX idx_contact_requests_subject;
