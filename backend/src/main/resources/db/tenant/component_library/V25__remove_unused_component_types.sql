-- Remove unused component types and related components

DELETE c
FROM components c
JOIN component_types ct ON ct.id = c.component_type_id
WHERE ct.uid IN (
    'HeaderComponent',
    'FooterComponent',
    'CategoryNavigationComponent',
    'CustomerReviewComponent',
    'ImageMapComponent',
    'PricingTableComponent'
);

DELETE FROM component_types
WHERE uid IN (
    'HeaderComponent',
    'FooterComponent',
    'CategoryNavigationComponent',
    'CustomerReviewComponent',
    'ImageMapComponent',
    'PricingTableComponent'
);
