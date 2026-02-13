-- Make default page templates tenant-editable
UPDATE page_templates
SET is_system = FALSE
WHERE uid IN (
    'LandingPageTemplate',
    'ContentPageTemplate',
    'CategoryPageTemplate',
    'ProductDetailsPageTemplate',
    'SearchResultsPageTemplate',
    'AccountPageTemplate',
    'LoginPageTemplate',
    'ErrorPageTemplate',
    'NotFoundPageTemplate'
);
