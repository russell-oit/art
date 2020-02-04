-- Upgrade script from ART 4.10 to ART 4.11

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.11-snapshot';
