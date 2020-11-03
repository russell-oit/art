-- Upgrade script from ART 4.18 to ART 5.0

-- CHANGES:
-- update database version
-- add schedule_id column for pipeline
-- add quartz calendar names column for pipeline
-- increase size of username column in datasources
-- add start conditions table
-- add start conditions permission
-- add start condition id columns

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
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='5.0-snapshot';

-- add schedule_id column for pipeline
ALTER TABLE ART_PIPELINES ADD SCHEDULE_ID INTEGER;

-- add quartz calendar names column for pipeline
ALTER TABLE ART_PIPELINES ADD QUARTZ_CALENDAR_NAMES VARCHAR(100);

-- increase size of username column in datasources
ALTER TABLE ART_DATABASES MODIFY USERNAME VARCHAR(100);

-- add start conditions table
CREATE TABLE ART_START_CONDITIONS
(
	START_CONDITION_ID INTEGER NOT NULL,
	NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	RETRY_DELAY_MINS INTEGER,
	RETRY_ATTEMPTS INTEGER,
	START_CONDITION CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_stcdn_pk PRIMARY KEY(START_CONDITION_ID),
	CONSTRAINT art_stcdn_uq_nm UNIQUE(NAME)
);

-- add start conditions permission
INSERT INTO ART_PERMISSIONS VALUES(33, 'configure_start_conditions');

-- add start condition id columns
ALTER TABLE ART_JOBS ADD START_CONDITION_ID INTEGER;
ALTER TABLE ART_PIPELINES ADD START_CONDITION_ID INTEGER;
