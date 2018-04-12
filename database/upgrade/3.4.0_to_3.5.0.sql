-- Upgrade script from ART 3.4 to ART 3.5

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.5-snapshot';
