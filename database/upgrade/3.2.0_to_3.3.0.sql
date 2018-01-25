-- Upgrade script from ART 3.2 to ART 3.3

-- CHANGES:
-- update database version
-- add job error notification to column
-- add reference records for new report types
-- add fields for password encryptor type
-- rename art queries database_id field

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.3-snapshot';

-- add job error notification to column
ALTER TABLE ART_JOBS ADD ERROR_EMAIL_TO VARCHAR(500);

-- add reference records for new report types
INSERT INTO ART_REPORT_TYPES VALUES (158,'ReportEngine');
INSERT INTO ART_REPORT_TYPES VALUES (159,'ReportEngine: File');

-- add fields for password encryptor type
ALTER TABLE ART_ENCRYPTORS ADD OPEN_PASSWORD VARCHAR(200);
ALTER TABLE ART_ENCRYPTORS ADD MODIFY_PASSWORD VARCHAR(200);

-- rename art queries database_id field
ALTER TABLE ART_QUERIES ADD DATASOURCE_ID INTEGER;
UPDATE ART_QUERIES SET DATASOURCE_ID=DATABASE_ID;
ALTER TABLE ART_QUERIES DROP COLUMN DATABASE_ID;
UPDATE ART_QUERIES SET DATASOURCE_ID=NULL WHERE DATASOURCE_ID=0;
