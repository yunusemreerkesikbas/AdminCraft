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
