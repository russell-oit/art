-- Upgrade script from ART 3.3 to ART 3.4

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.4-snapshot';
