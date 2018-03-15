-- Upgrade script from ART 3.3 to ART 3.4

-- CHANGES:
-- update database version
-- add time zone columns
-- add jwt token expiry column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.4-snapshot';

-- add time zone columns
ALTER TABLE ART_JOBS ADD TIME_ZONE VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD TIME_ZONE VARCHAR(50);

-- add jwt token expiry column
ALTER TABLE ART_SETTINGS ADD JWT_TOKEN_EXPIRY INTEGER;