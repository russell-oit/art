-- Upgrade script from ART 3.4 to ART 3.5

-- CHANGES:
-- update database version
-- add use groovy column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.5-snapshot';

-- add use groovy column
ALTER TABLE ART_QUERIES ADD USE_GROOVY INTEGER;
UPDATE ART_QUERIES SET USE_GROOVY=0;