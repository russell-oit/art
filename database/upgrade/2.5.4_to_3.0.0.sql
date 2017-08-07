-- Upgrade script from ART 2.5.4 to ART 3.0

-- CHANGES:
-- update database version
-- reset x_axis_label column for non-graph queries
-- decrease size of x_axis_label column
-- allow datasources to be disabled
-- add reference table for report types
-- add reference table for job types
-- change active_status fields from varchar to integer
-- increase size of username columns
-- rename update_time column
-- increase size of log_type column
-- change update_date columns to timestamps
-- add user_id columns
-- change can_change_password field from varchar to integer
-- rename hashing_algorithm column
-- reset lov group to id -1 and update default_query_groups column values
-- add creation_date columns
-- and many others...

-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
--
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>
--
-- for oracle, postgresql, replace the SUBSTRING keyword with SUBSTR
--
-- for sql server, mysql, replace TIMESTAMP with DATETIME


-- ------------------------------------------------


-- ***************
-- IMPORTANT

-- after running this upgrade script, ALSO RUN the tables_xxx.sql script for your database 
-- (found in the quartz directory). this is not the usual process for upgrades.

-- *****************


-- update database version
DROP TABLE ART_SETTINGS;
CREATE TABLE ART_DATABASE_VERSION
(
	DATABASE_VERSION VARCHAR(50),
	UPGRADED INTEGER
);
-- insert database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.0-rc3', 0);

-- reset x_axis_label column for non-graph queries
UPDATE ART_QUERIES SET X_AXIS_LABEL='' WHERE QUERY_TYPE>=0;

-- decrease size of x_axis_label column
UPDATE ART_QUERIES SET X_AXIS_LABEL=SUBSTRING(X_AXIS_LABEL,1,50);
ALTER TABLE ART_QUERIES MODIFY X_AXIS_LABEL VARCHAR(50);

-- allow datasources to be disabled
ALTER TABLE ART_DATABASES ADD ACTIVE INTEGER;
UPDATE ART_DATABASES SET ACTIVE=1;

-- add description column
ALTER TABLE ART_DATABASES ADD DESCRIPTION VARCHAR(200);

-- change active_status fields from varchar to integer
ALTER TABLE ART_USERS ADD ACTIVE INTEGER;
UPDATE ART_USERS SET ACTIVE=0;
UPDATE ART_USERS SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;
ALTER TABLE ART_USERS DROP COLUMN ACTIVE_STATUS;

ALTER TABLE ART_JOBS ADD ACTIVE INTEGER;
UPDATE ART_JOBS SET ACTIVE=0;
UPDATE ART_JOBS SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;
ALTER TABLE ART_JOBS DROP COLUMN ACTIVE_STATUS;

-- rename update_time column
ALTER TABLE ART_LOGS ADD LOG_DATE TIMESTAMP;
UPDATE ART_LOGS SET LOG_DATE=UPDATE_TIME;
ALTER TABLE ART_LOGS DROP COLUMN UPDATE_TIME;

-- increase size of log_type column
ALTER TABLE ART_LOGS MODIFY LOG_TYPE VARCHAR(50);

-- increase size of username columns
ALTER TABLE ART_USERS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_DATABASES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_ADMIN_PRIVILEGES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_QUERIES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_QUERY_GROUPS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_RULES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOBS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOBS_AUDIT MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_LOGS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_JOBS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_GROUP_ASSIGNMENT MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOB_ARCHIVES MODIFY USERNAME VARCHAR(50);

-- change update_date columns to timestamps
 ALTER TABLE ART_USERS MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_DATABASES MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_QUERIES MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_USER_QUERIES DROP COLUMN UPDATE_DATE;
 ALTER TABLE ART_QUERY_FIELDS MODIFY UPDATE_DATE TIMESTAMP;
 
-- change jobs start and end dates to timestamps
ALTER TABLE ART_JOBS MODIFY START_DATE TIMESTAMP;
ALTER TABLE ART_JOBS MODIFY END_DATE TIMESTAMP;

-- add user_id columns
ALTER TABLE ART_USERS ADD USER_ID INTEGER;
ALTER TABLE ART_ADMIN_PRIVILEGES ADD USER_ID INTEGER;
ALTER TABLE ART_USER_QUERIES ADD USER_ID INTEGER;
ALTER TABLE ART_USER_QUERY_GROUPS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_RULES ADD USER_ID INTEGER;
ALTER TABLE ART_JOBS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_JOBS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_GROUP_ASSIGNMENT ADD USER_ID INTEGER;
ALTER TABLE ART_JOB_ARCHIVES ADD USER_ID INTEGER;
ALTER TABLE ART_JOBS_AUDIT ADD USER_ID INTEGER;

-- change can_change_password field from varchar to integer
ALTER TABLE ART_USERS ADD TMP_CAN_CHANGE_PASSWORD VARCHAR(1);
UPDATE ART_USERS SET TMP_CAN_CHANGE_PASSWORD=CAN_CHANGE_PASSWORD;
ALTER TABLE ART_USERS DROP COLUMN CAN_CHANGE_PASSWORD;
ALTER TABLE ART_USERS ADD CAN_CHANGE_PASSWORD INTEGER;
UPDATE ART_USERS SET CAN_CHANGE_PASSWORD=0;
UPDATE ART_USERS SET CAN_CHANGE_PASSWORD=1 WHERE TMP_CAN_CHANGE_PASSWORD='Y' OR TMP_CAN_CHANGE_PASSWORD IS NULL;
ALTER TABLE ART_USERS DROP COLUMN TMP_CAN_CHANGE_PASSWORD;

-- rename hashing_algorithm column
ALTER TABLE ART_USERS ADD PASSWORD_ALGORITHM VARCHAR(20);
UPDATE ART_USERS SET PASSWORD_ALGORITHM=HASHING_ALGORITHM;
ALTER TABLE ART_USERS DROP COLUMN HASHING_ALGORITHM;

-- reset lov group to id -1 and update default_query_groups column values
UPDATE ART_QUERY_GROUPS SET QUERY_GROUP_ID=-1 WHERE QUERY_GROUP_ID=0;
UPDATE ART_QUERIES SET QUERY_GROUP_ID=-1 WHERE QUERY_GROUP_ID=0;
UPDATE ART_QUERIES SET QUERY_TYPE=119 WHERE QUERY_GROUP_ID=-1 AND QUERY_TYPE=0;
UPDATE ART_USERS SET DEFAULT_QUERY_GROUP=0 WHERE DEFAULT_QUERY_GROUP=-1;
UPDATE ART_USER_GROUPS SET DEFAULT_QUERY_GROUP=0 WHERE DEFAULT_QUERY_GROUP=-1;

-- add creation_date columns
ALTER TABLE ART_USERS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERIES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_USER_GROUPS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_USER_GROUPS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_DATABASES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERY_GROUPS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERY_GROUPS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_JOB_SCHEDULES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_JOB_SCHEDULES ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_JOBS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_JOBS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_RULES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_RULES ADD UPDATE_DATE TIMESTAMP;

-- add created by and updated_by columns
ALTER TABLE ART_USERS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_USERS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_DATABASES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_DATABASES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_QUERY_GROUPS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_QUERY_GROUPS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_QUERIES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_QUERIES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_RULES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_RULES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_JOBS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_JOBS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_USER_GROUPS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_USER_GROUPS ADD UPDATED_BY VARCHAR(50);

-- change uses_rules column from varchar to integer
ALTER TABLE ART_QUERIES ADD TMP_USES_RULES VARCHAR(1);
UPDATE ART_QUERIES SET TMP_USES_RULES=USES_RULES;
ALTER TABLE ART_QUERIES DROP COLUMN USES_RULES;
ALTER TABLE ART_QUERIES ADD USES_RULES INTEGER;
UPDATE ART_QUERIES SET USES_RULES=0;
UPDATE ART_QUERIES SET USES_RULES=1 WHERE TMP_USES_RULES='Y' OR TMP_USES_RULES IS NULL;
ALTER TABLE ART_QUERIES DROP COLUMN TMP_USES_RULES;

-- add active and hidden columns for art_queries
ALTER TABLE ART_QUERIES ADD ACTIVE INTEGER;
UPDATE ART_QUERIES SET ACTIVE=0;
UPDATE ART_QUERIES SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;
ALTER TABLE ART_QUERIES ADD HIDDEN INTEGER;
UPDATE ART_QUERIES SET HIDDEN=0;
UPDATE ART_QUERIES SET HIDDEN=1 WHERE ACTIVE_STATUS='H';
ALTER TABLE ART_QUERIES DROP COLUMN ACTIVE_STATUS;

-- change show_parameters column from varchar to integer
ALTER TABLE ART_QUERIES ADD PARAMETERS_IN_OUTPUT INTEGER;
UPDATE ART_QUERIES SET PARAMETERS_IN_OUTPUT=0;
UPDATE ART_QUERIES SET PARAMETERS_IN_OUTPUT=1 WHERE SHOW_PARAMETERS='Y' OR SHOW_PARAMETERS='A';
ALTER TABLE ART_QUERIES DROP COLUMN SHOW_PARAMETERS;

-- decrease size of message column
UPDATE ART_LOGS SET MESSAGE=SUBSTRING(MESSAGE,1,500);
ALTER TABLE ART_LOGS MODIFY MESSAGE VARCHAR(500);

-- add jndi column
ALTER TABLE ART_DATABASES ADD JNDI INTEGER;
UPDATE ART_DATABASES SET JNDI=0;
UPDATE ART_DATABASES SET JNDI=1 WHERE DRIVER='' OR DRIVER IS NULL;

-- add schedule_id column
ALTER TABLE ART_JOB_SCHEDULES ADD SCHEDULE_ID INTEGER;

-- add schedule description column
ALTER TABLE ART_JOB_SCHEDULES ADD DESCRIPTION VARCHAR(200);

-- change open in new window column to integer
ALTER TABLE ART_DRILLDOWN_QUERIES ADD OLD_OPEN_IN_NEW_WINDOW VARCHAR(1);
UPDATE ART_DRILLDOWN_QUERIES SET OLD_OPEN_IN_NEW_WINDOW=OPEN_IN_NEW_WINDOW;
ALTER TABLE ART_DRILLDOWN_QUERIES DROP COLUMN OPEN_IN_NEW_WINDOW;
ALTER TABLE ART_DRILLDOWN_QUERIES ADD OPEN_IN_NEW_WINDOW INTEGER;
UPDATE ART_DRILLDOWN_QUERIES SET OPEN_IN_NEW_WINDOW=0;
UPDATE ART_DRILLDOWN_QUERIES SET OPEN_IN_NEW_WINDOW=1 WHERE OLD_OPEN_IN_NEW_WINDOW='Y';
ALTER TABLE ART_DRILLDOWN_QUERIES DROP COLUMN OLD_OPEN_IN_NEW_WINDOW;

-- add drilldown id column
ALTER TABLE ART_DRILLDOWN_QUERIES ADD DRILLDOWN_ID INTEGER;

-- add rule_id columns
ALTER TABLE ART_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_QUERY_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_USER_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_USER_GROUP_RULES ADD RULE_ID INTEGER;

-- add data type column
ALTER TABLE ART_RULES ADD DATA_TYPE VARCHAR(30);
UPDATE ART_RULES SET DATA_TYPE='Varchar';

-- add query rule id column
ALTER TABLE ART_QUERY_RULES ADD QUERY_RULE_ID INTEGER;

-- add rule value fields
ALTER TABLE ART_USER_RULES ADD RULE_VALUE_KEY VARCHAR(50);
ALTER TABLE ART_USER_GROUP_RULES ADD RULE_VALUE_KEY VARCHAR(50);

-- change job_type column to varchar
ALTER TABLE ART_JOBS ADD OLD_JOB_TYPE INTEGER;
UPDATE ART_JOBS SET OLD_JOB_TYPE=JOB_TYPE;
ALTER TABLE ART_JOBS DROP COLUMN JOB_TYPE;
ALTER TABLE ART_JOBS ADD JOB_TYPE VARCHAR(50);
UPDATE ART_JOBS SET JOB_TYPE='Alert' WHERE OLD_JOB_TYPE=1;
UPDATE ART_JOBS SET JOB_TYPE='EmailAttachment' WHERE OLD_JOB_TYPE=2;
UPDATE ART_JOBS SET JOB_TYPE='Publish' WHERE OLD_JOB_TYPE=3;
UPDATE ART_JOBS SET JOB_TYPE='JustRun' WHERE OLD_JOB_TYPE=4;
UPDATE ART_JOBS SET JOB_TYPE='EmailInline' WHERE OLD_JOB_TYPE=5;
UPDATE ART_JOBS SET JOB_TYPE='CondEmailAttachment' WHERE OLD_JOB_TYPE=6;
UPDATE ART_JOBS SET JOB_TYPE='CondEmailInline' WHERE OLD_JOB_TYPE=7;
UPDATE ART_JOBS SET JOB_TYPE='CondPublish' WHERE OLD_JOB_TYPE=8;
UPDATE ART_JOBS SET JOB_TYPE='CacheAppend' WHERE OLD_JOB_TYPE=9;
UPDATE ART_JOBS SET JOB_TYPE='CacheInsert' WHERE OLD_JOB_TYPE=10;
ALTER TABLE ART_JOBS DROP COLUMN OLD_JOB_TYPE;

-- add migrated column
ALTER TABLE ART_QUERY_FIELDS ADD MIGRATED INTEGER;

-- create new parameter tables
CREATE TABLE ART_PARAMETERS
(	
	PARAMETER_ID INTEGER NOT NULL,		
	NAME  VARCHAR(60),
	DESCRIPTION VARCHAR(50),
	PARAMETER_TYPE VARCHAR(30),           
	PARAMETER_LABEL     VARCHAR(50),
	HELP_TEXT            VARCHAR(500),
	DATA_TYPE         VARCHAR(30),
	DEFAULT_VALUE     VARCHAR(1000),
	DEFAULT_VALUE_REPORT_ID INTEGER,
	HIDDEN INTEGER,
	SHARED INTEGER,
	USE_LOV INTEGER, 
	LOV_REPORT_ID  INTEGER,
	USE_RULES_IN_LOV INTEGER,	
	DRILLDOWN_COLUMN_INDEX INTEGER,
	USE_DIRECT_SUBSTITUTION INTEGER,
	PARAMETER_OPTIONS VARCHAR(4000),
	PARAMETER_DATE_FORMAT VARCHAR(100),
	PLACEHOLDER_TEXT VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ap_pk PRIMARY KEY (PARAMETER_ID)	
);

CREATE TABLE ART_REPORT_PARAMETERS
(	
	REPORT_PARAMETER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,	
	PARAMETER_ID INTEGER NOT NULL,	
	PARAMETER_POSITION INTEGER NOT NULL,
	CHAINED_PARENTS  VARCHAR(200),              
	CHAINED_DEPENDS VARCHAR(200),
	CONSTRAINT arp_pk PRIMARY KEY (REPORT_PARAMETER_ID)	
);

-- add last run details column
ALTER TABLE ART_JOBS ADD LAST_RUN_DETAILS VARCHAR(4000);
UPDATE ART_JOBS SET LAST_RUN_DETAILS=LAST_FILE_NAME;
ALTER TABLE ART_USER_JOBS ADD LAST_RUN_DETAILS VARCHAR(4000);
UPDATE ART_USER_JOBS SET LAST_RUN_DETAILS=LAST_FILE_NAME;

-- add last run message column
ALTER TABLE ART_JOBS ADD LAST_RUN_MESSAGE VARCHAR(100);
ALTER TABLE ART_USER_JOBS ADD LAST_RUN_MESSAGE VARCHAR(100);

-- add group column
ALTER TABLE ART_QUERIES ADD GROUP_COLUMN INTEGER;
UPDATE ART_QUERIES SET GROUP_COLUMN=QUERY_TYPE WHERE QUERY_TYPE>=1 AND QUERY_TYPE <=99;
UPDATE ART_QUERIES SET QUERY_TYPE=1 WHERE QUERY_TYPE>=1 AND QUERY_TYPE <=99;

-- change enable_audit field from varchar to integer
ALTER TABLE ART_JOBS ADD TMP_ENABLE_AUDIT VARCHAR(1);
UPDATE ART_JOBS SET TMP_ENABLE_AUDIT=ENABLE_AUDIT;
ALTER TABLE ART_JOBS DROP COLUMN ENABLE_AUDIT;
ALTER TABLE ART_JOBS ADD ENABLE_AUDIT INTEGER;
UPDATE ART_JOBS SET ENABLE_AUDIT=0;
UPDATE ART_JOBS SET ENABLE_AUDIT=1 WHERE TMP_ENABLE_AUDIT='Y' OR TMP_ENABLE_AUDIT IS NULL;
ALTER TABLE ART_JOBS DROP COLUMN TMP_ENABLE_AUDIT;

-- change allow_sharing field from varchar to integer
ALTER TABLE ART_JOBS ADD TMP_ALLOW_SHARING VARCHAR(1);
UPDATE ART_JOBS SET TMP_ALLOW_SHARING=ALLOW_SHARING;
ALTER TABLE ART_JOBS DROP COLUMN ALLOW_SHARING;
ALTER TABLE ART_JOBS ADD ALLOW_SHARING INTEGER;
UPDATE ART_JOBS SET ALLOW_SHARING=0;
UPDATE ART_JOBS SET ALLOW_SHARING=1 WHERE TMP_ALLOW_SHARING='Y' OR TMP_ALLOW_SHARING IS NULL;
ALTER TABLE ART_JOBS DROP COLUMN TMP_ALLOW_SHARING;

-- change allow_splitting field from varchar to integer
ALTER TABLE ART_JOBS ADD TMP_ALLOW_SPLITTING VARCHAR(1);
UPDATE ART_JOBS SET TMP_ALLOW_SPLITTING=ALLOW_SPLITTING;
ALTER TABLE ART_JOBS DROP COLUMN ALLOW_SPLITTING;
ALTER TABLE ART_JOBS ADD ALLOW_SPLITTING INTEGER;
UPDATE ART_JOBS SET ALLOW_SPLITTING=0;
UPDATE ART_JOBS SET ALLOW_SPLITTING=1 WHERE TMP_ALLOW_SPLITTING='Y' OR TMP_ALLOW_SPLITTING IS NULL;
ALTER TABLE ART_JOBS DROP COLUMN TMP_ALLOW_SPLITTING;

-- add cached datasource id field
ALTER TABLE ART_JOBS ADD CACHED_DATASOURCE_ID INTEGER;

-- add password algorithm field to art_databases
ALTER TABLE ART_DATABASES ADD PASSWORD_ALGORITHM VARCHAR(20);
UPDATE ART_DATABASES SET PASSWORD_ALGORITHM='ART';

-- add datasource type column
ALTER TABLE ART_DATABASES ADD DATASOURCE_TYPE VARCHAR(20);
UPDATE ART_DATABASES SET DATASOURCE_TYPE='JDBC';

-- drop xmla url, username and password fields
ALTER TABLE ART_QUERIES DROP COLUMN XMLA_URL;
ALTER TABLE ART_QUERIES DROP COLUMN XMLA_USERNAME;
ALTER TABLE ART_QUERIES DROP COLUMN XMLA_PASSWORD;

-- add logged_in_users table
CREATE TABLE ART_LOGGED_IN_USERS
(
	LOGGED_IN_USERS_ID VARCHAR(100) NOT NULL,
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	LOGIN_DATE TIMESTAMP NULL,
	IP_ADDRESS VARCHAR(50),
	CONSTRAINT alu_pk PRIMARY KEY(LOGGED_IN_USERS_ID)
);

-- add batch file field
ALTER TABLE ART_JOBS ADD BATCH_FILE VARCHAR(50);

-- add default_report_format field
ALTER TABLE ART_QUERIES ADD DEFAULT_REPORT_FORMAT VARCHAR(20);

-- increase size of datasource password column
ALTER TABLE ART_DATABASES MODIFY PASSWORD VARCHAR(100);

-- create ftp servers table
CREATE TABLE ART_FTP_SERVERS
(
	FTP_SERVER_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	SERVER VARCHAR(100),
	PORT INTEGER,
	FTP_USER VARCHAR(50),
	PASSWORD VARCHAR(100),
	REMOTE_DIRECTORY VARCHAR(200),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT afs_pk PRIMARY KEY(FTP_SERVER_ID),
	CONSTRAINT afs_name_uq UNIQUE(NAME)
);

-- add ftp server id to jobs
ALTER TABLE ART_JOBS ADD FTP_SERVER_ID INTEGER;

-- add secondary charts column
ALTER TABLE ART_QUERIES ADD SECONDARY_CHARTS VARCHAR(100);

-- add hidden columns column
ALTER TABLE ART_QUERIES ADD HIDDEN_COLUMNS VARCHAR(500);

-- add fixed file name field
ALTER TABLE ART_JOBS ADD FIXED_FILE_NAME VARCHAR(50);

-- add total columns column
ALTER TABLE ART_QUERIES ADD TOTAL_COLUMNS VARCHAR(500);

-- add column formats columns
ALTER TABLE ART_QUERIES ADD DATE_COLUMN_FORMAT VARCHAR(100);
ALTER TABLE ART_QUERIES ADD NUMBER_COLUMN_FORMAT VARCHAR(50);
ALTER TABLE ART_QUERIES ADD COLUMN_FORMATS VARCHAR(2000);

-- add locale column
ALTER TABLE ART_QUERIES ADD LOCALE VARCHAR(50);

-- add null number and string display columns
ALTER TABLE ART_QUERIES ADD NULL_NUMBER_DISPLAY VARCHAR(50);
ALTER TABLE ART_QUERIES ADD NULL_STRING_DISPLAY VARCHAR(50);

-- add fetch size column
ALTER TABLE ART_QUERIES ADD FETCH_SIZE INTEGER;

-- increase size of full name and email columns
ALTER TABLE ART_USERS MODIFY FULL_NAME VARCHAR(100);
ALTER TABLE ART_USERS MODIFY EMAIL VARCHAR(100);

-- set lov_query_id to 0 for parameters where use_lov is false
UPDATE ART_QUERY_FIELDS SET LOV_QUERY_ID = 0 WHERE USE_LOV = 'N';

-- increase size of contact person column
ALTER TABLE ART_QUERIES MODIFY CONTACT_PERSON VARCHAR(100);

-- increase size of datasource name column
ALTER TABLE ART_DATABASES MODIFY NAME VARCHAR(50);

-- increase size of datasource username column
ALTER TABLE ART_DATABASES MODIFY USERNAME VARCHAR(50);

-- increase size of report groups name column
ALTER TABLE ART_QUERY_GROUPS MODIFY NAME VARCHAR(50);

-- increase size of report groups description column
ALTER TABLE ART_QUERY_GROUPS MODIFY DESCRIPTION VARCHAR(100);

-- increase size of rule name column
ALTER TABLE ART_RULES MODIFY RULE_NAME VARCHAR(50);

-- increase size of rule description column
ALTER TABLE ART_RULES MODIFY SHORT_DESCRIPTION VARCHAR(100);

-- increase size of query rules rule name column
ALTER TABLE ART_QUERY_RULES MODIFY RULE_NAME VARCHAR(50);

-- increase size of user rules rule name column
ALTER TABLE ART_USER_RULES MODIFY RULE_NAME VARCHAR(50);

-- increase size of user rules rule value column
ALTER TABLE ART_USER_RULES MODIFY RULE_VALUE VARCHAR(100);

-- increase size of user group rules rule name column
ALTER TABLE ART_USER_GROUP_RULES MODIFY RULE_NAME VARCHAR(50);

-- increase size of user group rules rule value column
ALTER TABLE ART_USER_GROUP_RULES MODIFY RULE_VALUE VARCHAR(100);

-- increase size of logs ip column
ALTER TABLE ART_LOGS MODIFY IP VARCHAR(50);

-- increase size of user groups name column
ALTER TABLE ART_USER_GROUPS MODIFY NAME VARCHAR(50);

-- increase size of user groups description column
ALTER TABLE ART_USER_GROUPS MODIFY DESCRIPTION VARCHAR(100);

-- increase size of drill down title column
ALTER TABLE ART_DRILLDOWN_QUERIES MODIFY DRILLDOWN_TITLE VARCHAR(50);

-- increase size of drill down title column
ALTER TABLE ART_DRILLDOWN_QUERIES MODIFY DRILLDOWN_TEXT VARCHAR(50);

-- add report_options column
ALTER TABLE ART_QUERIES ADD REPORT_OPTIONS VARCHAR(4000);

-- add pdf_orientation column
ALTER TABLE ART_QUERIES ADD PAGE_ORIENTATION VARCHAR(20);

-- rename "number" parameter data type to "double"
UPDATE ART_QUERY_FIELDS SET PARAM_DATA_TYPE='Double' WHERE PARAM_DATA_TYPE='NUMBER';

-- change html output type for tabular drilldowns to htmlFancy
-- https://stackoverflow.com/questions/1293330/how-can-i-do-an-update-statement-with-join-in-sql
UPDATE ART_DRILLDOWN_QUERIES SET OUTPUT_FORMAT = 'htmlFancy' WHERE EXISTS(SELECT * FROM ART_QUERIES WHERE ART_QUERIES.QUERY_ID=ART_DRILLDOWN_QUERIES.DRILLDOWN_QUERY_ID AND ART_QUERIES.QUERY_TYPE=0 AND ART_DRILLDOWN_QUERIES.OUTPUT_FORMAT='html');


-- add reference table for report types
CREATE TABLE ART_REPORT_TYPES
(
	REPORT_TYPE INTEGER NOT NULL,
	DESCRIPTION VARCHAR(100),
	CONSTRAINT art_pk PRIMARY KEY(REPORT_TYPE)
);
-- insert report types
INSERT INTO ART_REPORT_TYPES VALUES (0,'Tabular');
INSERT INTO ART_REPORT_TYPES VALUES (1,'Group');
INSERT INTO ART_REPORT_TYPES VALUES (100,'Update Statement');
INSERT INTO ART_REPORT_TYPES VALUES (101,'Crosstab');
INSERT INTO ART_REPORT_TYPES VALUES (102,'Crosstab (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (103,'Tabular (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (110,'Dashboard');
INSERT INTO ART_REPORT_TYPES VALUES (111,'Text');
INSERT INTO ART_REPORT_TYPES VALUES (112,'JPivot: Mondrian');
INSERT INTO ART_REPORT_TYPES VALUES (113,'JPivot: Mondrian XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (114,'JPivot: SQL Server XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (115,'JasperReport: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (116,'JasperReport: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (117,'jXLS: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (118,'jXLS: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (119,'LOV: Dynamic');
INSERT INTO ART_REPORT_TYPES VALUES (120,'LOV: Static');
INSERT INTO ART_REPORT_TYPES VALUES (121,'Dynamic Job Recipients');
INSERT INTO ART_REPORT_TYPES VALUES (122,'FreeMarker');
INSERT INTO ART_REPORT_TYPES VALUES (123,'XDocReport: FreeMarker engine - Docx');
INSERT INTO ART_REPORT_TYPES VALUES (124,'XDocReport: Velocity engine - Docx');
INSERT INTO ART_REPORT_TYPES VALUES (125,'XDocReport: FreeMarker engine - ODT');
INSERT INTO ART_REPORT_TYPES VALUES (126,'XDocReport: Velocity engine - ODT');
INSERT INTO ART_REPORT_TYPES VALUES (127,'XDocReport: FreeMarker engine - PPTX');
INSERT INTO ART_REPORT_TYPES VALUES (128,'XDocReport: Velocity engine - PPTX');
INSERT INTO ART_REPORT_TYPES VALUES (129,'Dashboard: Gridstack');
INSERT INTO ART_REPORT_TYPES VALUES (130,'ReactPivot');
INSERT INTO ART_REPORT_TYPES VALUES (131,'Thymeleaf');
INSERT INTO ART_REPORT_TYPES VALUES (132,'PivotTable.js');
INSERT INTO ART_REPORT_TYPES VALUES (133,'PivotTable.js: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (134,'PivotTable.js: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (135,'Dygraphs');
INSERT INTO ART_REPORT_TYPES VALUES (136,'Dygraphs: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (137,'Dygraphs: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (138,'DataTables');
INSERT INTO ART_REPORT_TYPES VALUES (139,'DataTables: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (140,'DataTables: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (141,'Fixed Width');
INSERT INTO ART_REPORT_TYPES VALUES (142,'C3.js');
INSERT INTO ART_REPORT_TYPES VALUES (143,'Chart.js');
INSERT INTO ART_REPORT_TYPES VALUES (144,'Datamaps');
INSERT INTO ART_REPORT_TYPES VALUES (145,'Datamaps: File');
INSERT INTO ART_REPORT_TYPES VALUES (146,'Leaflet');
INSERT INTO ART_REPORT_TYPES VALUES (147,'OpenLayers');
INSERT INTO ART_REPORT_TYPES VALUES (148,'Tabular: Heatmap');
INSERT INTO ART_REPORT_TYPES VALUES (149,'Saiku: Report');
INSERT INTO ART_REPORT_TYPES VALUES (150,'Saiku: Connection');
INSERT INTO ART_REPORT_TYPES VALUES (151,'MongoDB');
INSERT INTO ART_REPORT_TYPES VALUES (-1,'Chart: XY');
INSERT INTO ART_REPORT_TYPES VALUES (-2,'Chart: Pie 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-3,'Chart: Horizontal Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-4,'Chart: Vertical Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-5,'Chart: Line');
INSERT INTO ART_REPORT_TYPES VALUES (-6,'Chart: Time Series');
INSERT INTO ART_REPORT_TYPES VALUES (-7,'Chart: Date Series');
INSERT INTO ART_REPORT_TYPES VALUES (-8,'Chart: Stacked Vertical Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-9,'Chart: Stacked Horizontal Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-10,'Chart: Speedometer');
INSERT INTO ART_REPORT_TYPES VALUES (-11,'Chart: Bubble Chart');
INSERT INTO ART_REPORT_TYPES VALUES (-12,'Chart: Heat Map');
INSERT INTO ART_REPORT_TYPES VALUES (-13,'Chart: Pie 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-14,'Chart: Vertical Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-15,'Chart: Stacked Vertical Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-16,'Chart: Horizontal Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-17,'Chart: Stacked Horizontal Bar 2D');

-- add reference table for access levels
CREATE TABLE ART_ACCESS_LEVELS
(
	ACCESS_LEVEL INTEGER NOT NULL,
	DESCRIPTION VARCHAR(50),
	CONSTRAINT aal_pk PRIMARY KEY(ACCESS_LEVEL)
);
-- insert access levels
INSERT INTO ART_ACCESS_LEVELS VALUES (0,'Normal User');
INSERT INTO ART_ACCESS_LEVELS VALUES (5,'Schedule User');
INSERT INTO ART_ACCESS_LEVELS VALUES (10,'Junior Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (30,'Mid Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (40,'Standard Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (80,'Senior Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (100,'Super Admin');

-- delete quartz tables
DROP TABLE QRTZ_FIRED_TRIGGERS;
DROP TABLE QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE QRTZ_SCHEDULER_STATE;
DROP TABLE QRTZ_LOCKS;
DROP TABLE QRTZ_SIMPLE_TRIGGERS;
DROP TABLE QRTZ_SIMPROP_TRIGGERS;
DROP TABLE QRTZ_CRON_TRIGGERS;
DROP TABLE QRTZ_BLOB_TRIGGERS;
DROP TABLE QRTZ_TRIGGERS;
DROP TABLE QRTZ_JOB_DETAILS;
DROP TABLE QRTZ_CALENDARS;

-- update job migrated to quartz status so that all jobs are recreated in the new quartz tables
UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ='N';
