-- Upgrade script from ART 3.11 to ART 4.0

-- CHANGES:
-- update database version
-- set migrated to quartz column to null

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.0';

-- set migrated to quartz column to null
UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ=NULL WHERE MIGRATED_TO_QUARTZ='X' OR MIGRATED_TO_QUARTZ='Y';
