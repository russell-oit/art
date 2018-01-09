-- Upgrade script from ART 3.2 to ART 3.3

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.3-snapshot';
