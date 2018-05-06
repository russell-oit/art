-- Upgrade script from ART 3.6 to ART 3.7

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.7-snapshot';

