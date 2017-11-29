-- Upgrade script from ART 3.0.1 to ART 3.1

-- CHANGES:
-- update database version
-- add lov use dynamic datasource column
-- increase size of output format columns
-- increase size of job param value column
-- increase size of query rules field name column
-- increase size of parameter default value column
-- increase size of job subject and fixed file name columns
-- add open and modify password fields for reports
-- add encryptors table
-- add email template column
-- add extra schedules column
-- decrease size of last file name column
-- add report source column
-- add source report id column
-- add holidays column
-- add job second column
-- add job schedule id column
-- increase size of description columns
-- rename rule short description column
-- create art_report_report_groups table
-- allow for use of shared job schedule holidays
-- add job year column
-- create destinations table
-- create job-destination table
-- add job sub-directory column
-- add use default value in jobs column


-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>

-- for sql server, mysql, replace TIMESTAMP with DATETIME

-- for oracle, postgresql, replace the SUBSTRING keyword with SUBSTR

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.1-snapshot';

-- insert custom upgrade setting for 3.1
INSERT INTO ART_CUSTOM_UPGRADES VALUES('3.1', 0);

-- add lov use dynamic datasource column
ALTER TABLE ART_QUERIES ADD LOV_USE_DYNAMIC_DATASOURCE INTEGER;
UPDATE ART_QUERIES SET LOV_USE_DYNAMIC_DATASOURCE=0;

-- increase size of output format columns
ALTER TABLE ART_DRILLDOWN_QUERIES MODIFY OUTPUT_FORMAT VARCHAR(50);
ALTER TABLE ART_JOBS MODIFY OUTPUT_FORMAT VARCHAR(50);
ALTER TABLE ART_QUERIES MODIFY DEFAULT_REPORT_FORMAT VARCHAR(50);

-- increase size of job param value column
ALTER TABLE ART_JOBS_PARAMETERS MODIFY PARAM_VALUE VARCHAR(4000);

-- increase size of query rules field name column
ALTER TABLE ART_QUERY_RULES MODIFY FIELD_NAME VARCHAR(100);

-- increase size of parameter default value column
ALTER TABLE ART_PARAMETERS MODIFY DEFAULT_VALUE VARCHAR(4000);

-- increase size of job subject and fixed file name columns
ALTER TABLE ART_JOBS MODIFY SUBJECT VARCHAR(1000);
ALTER TABLE ART_JOBS MODIFY FIXED_FILE_NAME VARCHAR(1000);

-- add csv report type
INSERT INTO ART_REPORT_TYPES VALUES (152,'CSV');

-- add open and modify password fields for reports
ALTER TABLE ART_QUERIES ADD OPEN_PASSWORD VARCHAR(100);
ALTER TABLE ART_QUERIES ADD MODIFY_PASSWORD VARCHAR(100);

-- add encryptors table
CREATE TABLE ART_ENCRYPTORS
(
	ENCRYPTOR_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	ENCRYPTOR_TYPE VARCHAR(50),
	AESCRYPT_PASSWORD VARCHAR(100),
	OPENPGP_PUBLIC_KEY_FILE VARCHAR(100),
	OPENPGP_PUBLIC_KEY_STRING CLOB,
	OPENPGP_SIGNING_KEY_FILE VARCHAR(100),
	OPENPGP_SIGNING_KEY_PASSPHRASE VARCHAR(1000),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ae_pk PRIMARY KEY(ENCRYPTOR_ID),
	CONSTRAINT ae_name_uq UNIQUE(NAME)
);

-- add encryptor id column
ALTER TABLE ART_QUERIES ADD ENCRYPTOR_ID INTEGER;

-- add email template column
ALTER TABLE ART_JOBS ADD EMAIL_TEMPLATE VARCHAR(100);

-- add extra schedules column
ALTER TABLE ART_JOBS ADD EXTRA_SCHEDULES CLOB;
ALTER TABLE ART_JOB_SCHEDULES ADD EXTRA_SCHEDULES CLOB;

-- decrease size of last file name column
UPDATE ART_JOBS SET LAST_FILE_NAME = SUBSTRING(LAST_FILE_NAME,1,200);
ALTER TABLE ART_JOBS MODIFY LAST_FILE_NAME VARCHAR(200);

-- add report source column
ALTER TABLE ART_QUERIES ADD REPORT_SOURCE CLOB;

-- add source report id column
ALTER TABLE ART_QUERIES ADD SOURCE_REPORT_ID INTEGER;

-- add holidays column
ALTER TABLE ART_JOBS ADD HOLIDAYS CLOB;
ALTER TABLE ART_JOB_SCHEDULES ADD HOLIDAYS CLOB;

-- add calendars column
ALTER TABLE ART_JOBS ADD QUARTZ_CALENDAR_NAMES VARCHAR(100);

-- add job second column
ALTER TABLE ART_JOBS ADD JOB_SECOND VARCHAR(100);
ALTER TABLE ART_JOB_SCHEDULES ADD JOB_SECOND VARCHAR(100);

-- add job schedule id column
ALTER TABLE ART_JOBS ADD SCHEDULE_ID INTEGER;

-- increase size of description columns
ALTER TABLE ART_PARAMETERS MODIFY DESCRIPTION VARCHAR(200);
ALTER TABLE ART_QUERY_GROUPS MODIFY DESCRIPTION VARCHAR(200);
ALTER TABLE ART_USER_GROUPS MODIFY DESCRIPTION VARCHAR(200);

-- rename rule short description column
ALTER TABLE ART_RULES ADD DESCRIPTION VARCHAR(200);
UPDATE ART_RULES SET DESCRIPTION=SHORT_DESCRIPTION;
ALTER TABLE ART_RULES DROP COLUMN SHORT_DESCRIPTION;

-- create art_report_report_groups table
CREATE TABLE ART_REPORT_REPORT_GROUPS
(
	REPORT_ID INTEGER NOT NULL,	
	REPORT_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT arrg_pk PRIMARY KEY(REPORT_ID, REPORT_GROUP_ID)	
);

-- create holidays table
CREATE TABLE ART_HOLIDAYS
(
	HOLIDAY_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	HOLIDAY_DEFINITION CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ah_pk PRIMARY KEY(HOLIDAY_ID),
	CONSTRAINT ah_name_uq UNIQUE(NAME)
);

-- create schedule-holiday table
CREATE TABLE ART_SCHEDULE_HOLIDAY_MAP
(
	SCHEDULE_ID INTEGER NOT NULL,
	HOLIDAY_ID INTEGER NOT NULL,
	CONSTRAINT ashm_pk PRIMARY KEY(SCHEDULE_ID, HOLIDAY_ID)
);

-- create job-holiday table
CREATE TABLE ART_JOB_HOLIDAY_MAP
(
	JOB_ID INTEGER NOT NULL,
	HOLIDAY_ID INTEGER NOT NULL,
	CONSTRAINT ajhm_pk PRIMARY KEY(JOB_ID, HOLIDAY_ID)
);

-- add job year column
ALTER TABLE ART_JOBS ADD JOB_YEAR VARCHAR(100);
ALTER TABLE ART_JOB_SCHEDULES ADD JOB_YEAR VARCHAR(100);

-- create destinations table
CREATE TABLE ART_DESTINATIONS
(
	DESTINATION_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	DESTINATION_TYPE VARCHAR(50),
	SERVER VARCHAR(100),
	PORT INTEGER,
	DESTINATION_USER VARCHAR(50),
	DESTINATION_PASSWORD VARCHAR(100),
	USER_DOMAIN VARCHAR(100),
	DESTINATION_PATH VARCHAR(1000),
	SUB_DIRECTORY VARCHAR(100),
	CREATE_DIRECTORIES INTEGER,
	DESTINATION_OPTIONS VARCHAR(4000),	
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ad_pk PRIMARY KEY(DESTINATION_ID),
	CONSTRAINT ad_name_uq UNIQUE(NAME)
);

-- create job-destination table
CREATE TABLE ART_JOB_DESTINATION_MAP
(
	JOB_ID INTEGER NOT NULL,
	DESTINATION_ID INTEGER NOT NULL,
	CONSTRAINT ajdm_pk PRIMARY KEY(JOB_ID, DESTINATION_ID)
);

-- add ftp server migrated column
ALTER TABLE ART_FTP_SERVERS ADD MIGRATED INTEGER;

-- add job sub-directory column
ALTER TABLE ART_JOBS ADD SUB_DIRECTORY VARCHAR(100);

-- add use default value in jobs column
ALTER TABLE ART_PARAMETERS ADD USE_DEFAULT_VALUE_IN_JOBS INTEGER;