-- Enable PostgreSQL trigram extension for fuzzy search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN index on product title and description for faster fuzzy search
CREATE INDEX IF NOT EXISTS idx_product_title_trgm ON products USING gin (LOWER(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_description_trgm ON products USING gin (LOWER(description) gin_trgm_ops);

-- Add comment explaining the fuzzy search capability
COMMENT ON INDEX idx_product_title_trgm IS 'Trigram index for fuzzy search on product titles';
COMMENT ON INDEX idx_product_description_trgm IS 'Trigram index for fuzzy search on product descriptions';

