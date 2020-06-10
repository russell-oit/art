-- Upgrade script from ART 4.16 to ART 4.17

-- CHANGES:
-- update database version
-- add manual column
-- create pipelines table


-- NOTES:
-- for sql server, if using SSMS, replace -- GO with GO
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
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_ppln_pk PRIMARY KEY(PIPELINE_ID),
	CONSTRAINT art_ppln_uq_nm UNIQUE(NAME)
);
