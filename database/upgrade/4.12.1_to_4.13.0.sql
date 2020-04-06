-- Upgrade script from ART 4.12.1 to ART 4.13

-- CHANGES:
-- update database version
-- set cached datasource id to null
-- set logs datasource id to null

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.13';

-- set cached datasource id to null
UPDATE ART_JOBS SET CACHED_DATASOURCE_ID=NULL WHERE CACHED_DATASOURCE_ID=0;

-- set logs datasource id to null
UPDATE ART_SETTINGS SET LOGS_DATASOURCE_ID=NULL WHERE LOGS_DATASOURCE_ID=0;
