-- Upgrade script from ART 4.0 to ART 4.1

-- CHANGES:
-- update database version
-- add developer comment column


-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.1-snapshot';

-- add developer comment column
ALTER TABLE ART_QUERIES ADD DEVELOPER_COMMENT VARCHAR(2000);
