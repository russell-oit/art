-- Upgrade script from ART 4.15 to ART 4.16

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.16';
