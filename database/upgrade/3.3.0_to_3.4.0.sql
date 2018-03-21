-- Upgrade script from ART 3.3 to ART 3.4

-- CHANGES:
-- update database version
-- add time zone columns
-- add jwt token expiry column
-- create saved parameters table

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.4-snapshot';

-- add time zone columns
ALTER TABLE ART_JOBS ADD TIME_ZONE VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD TIME_ZONE VARCHAR(50);

-- add jwt token expiry column
ALTER TABLE ART_SETTINGS ADD JWT_TOKEN_EXPIRY INTEGER;

-- create saved parameters table
CREATE TABLE ART_SAVED_PARAMETERS
(
	USER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,
	PARAM_NAME VARCHAR(60) NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);