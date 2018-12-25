-- Upgrade script from ART 4.0 to ART 4.1

-- CHANGES:
-- update database version
-- add developer comment column
-- set shared column to 0 where it's null
-- add user description column
-- add self service view report type
-- add self service reports permission


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
