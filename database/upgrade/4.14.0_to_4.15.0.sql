-- Upgrade script from ART 4.14 to ART 4.15

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.15-snapshot';
