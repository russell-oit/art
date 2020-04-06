-- Upgrade script from ART 4.13 to ART 4.14

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.14-snapshot';
