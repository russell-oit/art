-- Upgrade script from ART 4.10 to ART 4.11

-- CHANGES:
-- update database version
-- add add_null column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.11-snapshot';

-- add add_null column
ALTER TABLE ART_PARAMETERS ADD ALLOW_NULL INTEGER;
UPDATE ART_PARAMETERS SET ALLOW_NULL=0;
