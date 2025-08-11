-- Fix timestamp columns in service_providers table
ALTER TABLE IF EXISTS service_providers
  ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE
  USING created_at::TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE IF EXISTS service_providers
  ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE
  USING updated_at::TIMESTAMP WITHOUT TIME ZONE;
