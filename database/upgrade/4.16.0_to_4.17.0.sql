-- Upgrade script from ART 4.16 to ART 4.17

-- CHANGES:
-- update database version
-- add manual column


-- NOTES:
-- for sql server, if using SSMS, replace -- GO with GO
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.17-snapshot';

-- add manual column
ALTER TABLE ART_JOBS ADD MANUAL INTEGER;
-- GO
UPDATE ART_JOBS SET MANUAL=0;
