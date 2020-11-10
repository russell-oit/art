-- Upgrade script from ART 5.0 to ART 5.1

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='5.1-snapshot';
