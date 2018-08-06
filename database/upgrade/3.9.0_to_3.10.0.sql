-- Upgrade script from ART 3.9 to ART 3.10

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.10-snapshot';
