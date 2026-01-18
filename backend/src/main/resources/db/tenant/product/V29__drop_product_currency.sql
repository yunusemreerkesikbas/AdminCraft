-- V29: Remove currency column from products table
-- Currency is now managed at tenant level

ALTER TABLE products
    DROP COLUMN currency;
