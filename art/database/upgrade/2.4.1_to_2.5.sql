-- Upgrade script from ART 2.4.1 to ART 2.5
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . Change "admin level" for users to "access level"
--          . rename "object" to "query"
--
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='2.5' WHERE SETTING_NAME='database version';

-- Change "admin level" for users to "access level"
ALTER TABLE ART_USERS ADD ACCESS_LEVEL INTEGER;
UPDATE ART_USERS SET ACCESS_LEVEL=ADMIN_LEVEL;
ALTER TABLE ART_USERS DROP COLUMN ADMIN_LEVEL;

-- rename "object" to "query"
ALTER TABLE ART_USERS ADD DEFAULT_QUERY_GROUP INTEGER;
UPDATE ART_USERS SET DEFAULT_QUERY_GROUP=DEFAULT_OBJECT_GROUP;
ALTER TABLE ART_USERS DROP COLUMN DEFAULT_OBJECT_GROUP;

ALTER TABLE ART_USER_GROUPS ADD DEFAULT_QUERY_GROUP INTEGER;
UPDATE ART_USER_GROUPS SET DEFAULT_QUERY_GROUP=DEFAULT_OBJECT_GROUP;
ALTER TABLE ART_USER_GROUPS DROP COLUMN DEFAULT_OBJECT_GROUP;

ALTER TABLE ART_LOGS ADD QUERY_ID INTEGER;
UPDATE ART_LOGS SET QUERY_ID=OBJECT_ID;
ALTER TABLE ART_LOGS DROP COLUMN OBJECT_ID;



