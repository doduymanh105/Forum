ALTER TABLE post_entity DROP CONSTRAINT post_entity_status_check;

ALTER TABLE post_entity ADD CONSTRAINT post_entity_status_check
CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'PENDING', 'REJECTED'));