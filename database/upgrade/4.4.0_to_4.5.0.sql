-- Upgrade script from ART 4.4 to ART 4.5

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.5-snapshot';
