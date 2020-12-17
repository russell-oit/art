-- Upgrade script from ART 4.11 to ART 4.12

-- CHANGES:
-- update database version
-- add link report type
-- add link report columns


-- NOTES:
-- for sql server, if using SSMS, replace -- GO with GO
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.12';

-- add link report type
INSERT INTO ART_REPORT_TYPES VALUES (163,'Link');

-- add link report columns
ALTER TABLE ART_QUERIES ADD LINK VARCHAR(2000);
ALTER TABLE ART_QUERIES ADD OPEN_IN_NEW_WINDOW INTEGER;
-- GO
UPDATE ART_QUERIES SET OPEN_IN_NEW_WINDOW=0;
