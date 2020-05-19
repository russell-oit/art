-- Upgrade script from ART 4.14 to ART 4.15

-- CHANGES:
-- update database version
-- add max running column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.15-snapshot';

-- add max running column
ALTER TABLE ART_QUERIES ADD MAX_RUNNING INTEGER;
