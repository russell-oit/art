-- Upgrade script from ART 4.5 to ART 4.6

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.6';
