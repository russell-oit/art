-- Upgrade script from ART 4.18 to ART 5.0

-- CHANGES:
-- update database version
-- add schedule_id column for pipeline
-- add quartz calendar names column for pipeline

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='5.0-snapshot';

-- add schedule_id column for pipeline
ALTER TABLE ART_PIPELINES ADD SCHEDULE_ID INTEGER;

-- add quartz calendar names column for pipeline
ALTER TABLE ART_PIPELINES ADD QUARTZ_CALENDAR_NAMES VARCHAR(100);
