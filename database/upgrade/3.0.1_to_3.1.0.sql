-- Upgrade script from ART 3.0.1 to ART 3.1

-- CHANGES:
-- update database version
-- add lov use dynamic datasource column
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.1-snapshot';

-- add lov use dynamic datasource column
ALTER TABLE ART_QUERIES ADD LOV_USE_DYNAMIC_DATASOURCE INTEGER;
UPDATE ART_QUERIES SET LOV_USE_DYNAMIC_DATASOURCE=0;
