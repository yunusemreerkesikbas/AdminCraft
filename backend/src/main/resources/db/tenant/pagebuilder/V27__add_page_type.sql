ALTER TABLE pages ADD COLUMN page_type VARCHAR(20) DEFAULT 'CONTENT' AFTER status;

UPDATE pages p
JOIN page_templates pt ON p.template_id = pt.id
SET p.page_type = CASE pt.uid
    WHEN 'ProductDetailsPageTemplate' THEN 'PRODUCT'
    WHEN 'CategoryPageTemplate' THEN 'CATEGORY'
    WHEN 'SearchResultsPageTemplate' THEN 'SEARCH'
    WHEN 'LandingPageTemplate' THEN 'LANDING'
    WHEN 'ErrorPageTemplate' THEN 'ERROR'
    WHEN 'NotFoundPageTemplate' THEN 'ERROR'
    ELSE 'CONTENT'
END;

CREATE INDEX idx_page_type_status ON pages(page_type, status);
CREATE INDEX idx_page_is_home_status ON pages(is_home, status);
