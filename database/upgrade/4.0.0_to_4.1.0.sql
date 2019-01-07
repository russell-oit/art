-- Upgrade script from ART 4.0 to ART 4.1

-- CHANGES:
-- update database version
-- add developer comment column
-- set shared column to 0 where it's null
-- add user description column
-- add self service view report type
-- add self service reports permission
-- change options columns to clob
-- add datasource options field
-- add database protocol column
-- add database type column
-- add view report id column


-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.1-snapshot';

-- add developer comment column
ALTER TABLE ART_QUERIES ADD DEVELOPER_COMMENT VARCHAR(2000);

-- set shared column to 0 where it's null
UPDATE ART_PARAMETERS SET SHARED=0 WHERE SHARED IS NULL;

-- add user description column
ALTER TABLE ART_USERS ADD DESCRIPTION VARCHAR(500);

-- add self service view report type
INSERT INTO ART_REPORT_TYPES VALUES (161,'View');

-- add self service reports permission
INSERT INTO ART_PERMISSIONS VALUES(30, 'self_service_reports');

-- change options columns to clob
ALTER TABLE ART_QUERIES MODIFY REPORT_OPTIONS CLOB;
ALTER TABLE ART_PARAMETERS MODIFY PARAMETER_OPTIONS CLOB;
ALTER TABLE ART_DESTINATIONS MODIFY DESTINATION_OPTIONS CLOB;

-- add datasource options field
ALTER TABLE ART_DATABASES ADD DATASOURCE_OPTIONS CLOB;

-- add database protocol column
ALTER TABLE ART_DATABASES ADD DATABASE_PROTOCOL VARCHAR(50);

-- add database type column
ALTER TABLE ART_DATABASES ADD DATABASE_TYPE VARCHAR(100);

-- add view report id column
ALTER TABLE ART_QUERIES ADD VIEW_REPORT_ID INTEGER;
