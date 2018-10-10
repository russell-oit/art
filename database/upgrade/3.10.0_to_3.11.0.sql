-- Upgrade script from ART 3.10 to ART 3.11

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.11-snapshot';
