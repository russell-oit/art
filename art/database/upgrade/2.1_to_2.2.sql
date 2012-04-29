-- Upgrade script from ART 2.1 to ART 2.2
--
-- Purpose: create/update the tables needed to 
--          . support art settings
--          . enlarge message column of art_logs table
--          . use bcrypt for password encryption
--          . allow query to include parameters in output by default
--          . enlarge test_sql column of art_databases table
--
-- NOTE:
-- this script will work as is for mysql, oracle
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN <column name> <data type>
-- for hsqldb you may need to run an explicit commit before the insert and update statements
-- ------------------------------------------------


-- create art settings table
CREATE TABLE ART_SETTINGS
(  
	SETTING_NAME VARCHAR(50) NOT NULL,
	SETTING_VALUE VARCHAR(2000),  
	PRIMARY KEY (SETTING_NAME)
);

-- insert database version setting
INSERT INTO ART_SETTINGS (SETTING_NAME,SETTING_VALUE) VALUES('database version','2.2');

-- enlarge message column of art_logs table
ALTER TABLE ART_LOGS MODIFY MESSAGE VARCHAR(4000);

-- enlarge password field to accomodate bcrypt passwords
ALTER TABLE ART_USERS MODIFY PASSWORD VARCHAR(200);

-- enlarge test_sql column of art_databases table
ALTER TABLE ART_DATABASES MODIFY TEST_SQL VARCHAR(60);

-- add hasing algorithm to accomodate bcrypt passwords
ALTER TABLE ART_USERS ADD HASHING_ALGORITHM VARCHAR(20);

-- set hashing algorithm to the current default
UPDATE ART_USERS SET HASHING_ALGORITHM='MD5';

-- add parameters in output field for queries
ALTER TABLE ART_QUERIES ADD SHOW_PARAMETERS VARCHAR(1);

-- remove object type column from art_all_sources
ALTER TABLE ART_ALL_SOURCES DROP COLUMN OBJECT_TYPE;
