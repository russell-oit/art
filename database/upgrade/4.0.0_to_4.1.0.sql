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
-- add self service options field
-- increase size of report name field


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

-- insert custom upgrade record
INSERT INTO ART_CUSTOM_UPGRADES VALUES('4.1', 0);

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

-- add self service options field
ALTER TABLE ART_QUERIES ADD SELF_SERVICE_OPTIONS CLOB;

-- increase size of report name field
ALTER TABLE ART_QUERIES MODIFY NAME VARCHAR(100);
ALTER TABLE ART_JOBS MODIFY JOB_NAME VARCHAR(100);

-- add created by id column
ALTER TABLE ART_QUERIES ADD CREATED_BY_ID INTEGER;

-- re-create some tables
CREATE TABLE ART_USER_USERGROUP_MAP
(
	USER_ID INTEGER NOT NULL,	
	USER_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT art_uugm_pk PRIMARY KEY(USER_ID, USER_GROUP_ID)	
);

CREATE TABLE ART_USER_REPORT_MAP
(
	USER_ID INTEGER NOT NULL,	
	REPORT_ID INTEGER NOT NULL,
	CONSTRAINT art_urm_pk PRIMARY KEY(USER_ID, REPORT_ID)	
);

CREATE TABLE ART_USER_REPORTGROUP_MAP
(
	USER_ID INTEGER NOT NULL,	
	REPORT_GROUP_ID INTEGER NOT NULL,        
	CONSTRAINT art_urgm_pk PRIMARY KEY(USER_ID, REPORT_GROUP_ID)	
);

