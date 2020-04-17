-- Upgrade script from ART 4.13 to ART 4.14

-- CHANGES:
-- update database version
-- add multiple_files column


-- NOTES:
-- for sql server, if using SSMS, replace -- GO with GO
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.14-snapshot';

-- add multiple_files column
ALTER TABLE ART_PARAMETERS ADD MULTIPLE_FILES INTEGER;
-- GO
UPDATE ART_PARAMETERS SET MULTIPLE_FILES=0;
