-- Upgrade script from ART 3.0.1 to ART 3.1

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.1-snapshot';
