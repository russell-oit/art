-- Upgrade script from ART 3.8 to ART 3.9

-- CHANGES:
-- update database version
-- add pre and post run report fields

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.9';

-- add pre and post run report fields
ALTER TABLE ART_JOBS ADD PRE_RUN_REPORT VARCHAR(50);
ALTER TABLE ART_JOBS ADD POST_RUN_REPORT VARCHAR(50);
