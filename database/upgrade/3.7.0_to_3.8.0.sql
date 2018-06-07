-- Upgrade script from ART 3.7 to ART 3.8

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.8-snapshot';
