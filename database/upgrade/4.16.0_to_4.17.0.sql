-- Upgrade script from ART 4.16 to ART 4.17

-- CHANGES:
-- update database version
-- add manual column
-- create pipelines table
-- add configure_pipelines permission
-- create pipelines_running_jobs table
-- add report groups hidden column


-- NOTES:
-- for sql server, if using SSMS, replace -- GO with GO

-- for sql server, mysql replace TIMESTAMP with DATETIME
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.17-snapshot';

-- add manual column
ALTER TABLE ART_JOBS ADD MANUAL INTEGER;
-- GO
UPDATE ART_JOBS SET MANUAL=0;

-- create pipelines table
CREATE TABLE ART_PIPELINES
(
	PIPELINE_ID INTEGER NOT NULL,
	NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	SERIAL VARCHAR(100),
	CONTINUE_ON_ERROR INTEGER,
	CANCELLED INTEGER,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_ppln_pk PRIMARY KEY(PIPELINE_ID),
	CONSTRAINT art_ppln_uq_nm UNIQUE(NAME)
);

-- add configure_pipelines permission
INSERT INTO ART_PERMISSIONS VALUES(32, 'configure_pipelines');

-- create pipelines_running_jobs table
CREATE TABLE ART_PIPELINE_RUNNING_JOBS
(
	PIPELINE_ID INTEGER NOT NULL,
	JOB_ID INTEGER NOT NULL
);

-- add report groups hidden column
ALTER TABLE ART_QUERY_GROUPS ADD HIDDEN INTEGER;
-- GO
UPDATE ART_QUERY_GROUPS SET HIDDEN=0;
