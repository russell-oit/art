-- Upgrade script from ART 3.2 to ART 3.3

-- CHANGES:
-- update database version
-- add job error notification to column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.3-snapshot';

-- add job error notification to column
ALTER TABLE ART_JOBS ADD ERROR_EMAIL_TO VARCHAR(500);
