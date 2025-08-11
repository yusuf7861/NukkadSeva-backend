-- Migration to fix null values in updated_at column
-- This script updates all null updated_at values to match created_at values

UPDATE provider
SET updated_at = created_at
WHERE updated_at IS NULL;

-- Add a constraint to prevent future null values if not already present
-- Note: This might fail if the constraint already exists, which is fine
ALTER TABLE provider
ALTER COLUMN updated_at SET NOT NULL;
