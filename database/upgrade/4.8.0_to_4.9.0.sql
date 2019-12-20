-- Upgrade script from ART 4.8 to ART 4.9

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.9-snapshot';
