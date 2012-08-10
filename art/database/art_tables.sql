-- Create the ART Repository, version 2.3

-- IMPORTANT:
-- after running this script, ALSO RUN the tables_xxx.sql script for your database (found in the quartz directory)

-- NOTES:
-- for sql server, replace "timestamp" data type with "datetime"
-- for sql server 2000/2005 also replace "date" data type with "datetime"

-- UPGRADING:
-- if you are upgrading, don't use this script. run the scripts available in the upgrade directory
-- run the scripts one at a time to upgrade to newer versions. e.g. from 2.0 to 2.1, then 2.1 to 2.2 etc.

-- if you want to create a new repository in place of an existing one, uncomment the DROP statements
-- you can use a text editor to replace "-- DROP" with "DROP" to accomplish this

-- ------------------------------------------------


-- ----DELETE TABLES----

-- DROP TABLE ART_SETTINGS;
-- DROP TABLE ART_ADMIN_PRIVILEGES;
-- DROP TABLE ART_USER_QUERIES;
-- DROP TABLE ART_USER_QUERY_GROUPS;
-- DROP TABLE ART_QUERY_FIELDS;
-- DROP TABLE ART_ALL_SOURCES;
-- DROP TABLE ART_QUERY_RULES;
-- DROP TABLE ART_USER_RULES;
-- DROP TABLE ART_JOBS_PARAMETERS;
-- DROP TABLE ART_JOBS_AUDIT;
-- DROP TABLE ART_SHARED_JOBS;
-- DROP TABLE ART_USER_GROUP_ASSIGNMENT;
-- DROP TABLE ART_USER_GROUP_QUERIES;
-- DROP TABLE ART_USER_GROUP_GROUPS;
-- DROP TABLE ART_USER_GROUP_JOBS;
-- DROP TABLE ART_DRILLDOWN_QUERIES;

-- DROP TABLE ART_JOBS;
-- DROP TABLE ART_QUERIES;

-- DROP TABLE ART_USER_GROUPS;
-- DROP TABLE ART_JOB_SCHEDULES;
-- DROP TABLE ART_LOGS;
-- DROP TABLE ART_RULES;
-- DROP TABLE ART_QUERY_GROUPS;
-- DROP TABLE ART_DATABASES;
-- DROP TABLE ART_USERS;

-- ----DELETE TABLES----


-- ART_SETTINGS
-- Stores miscellaneous settings

CREATE TABLE ART_SETTINGS
(  
	SETTING_NAME VARCHAR(50) NOT NULL,
	SETTING_VALUE VARCHAR(2000),  
	PRIMARY KEY (SETTING_NAME)
);
-- insert database version
INSERT INTO ART_SETTINGS (SETTING_NAME,SETTING_VALUE) VALUES('database version','2.3');


-- ART_USERS 
-- Stores user info

-- ADMIN_LEVEL: 0= normal user, 5 = normal user who can schedule jobs
-- 10 = junior admin, 30 = mid admin, 40 = admin, 80 = senior admin, 100 = super admin

CREATE TABLE ART_USERS
(
	USERNAME    VARCHAR(15) NOT NULL PRIMARY KEY,
	PASSWORD    VARCHAR(200) NOT NULL,
	HASHING_ALGORITHM VARCHAR(20) NOT NULL,
	FULL_NAME   VARCHAR(40),  
	EMAIL       VARCHAR(40),    
	ADMIN_LEVEL        INTEGER,
	DEFAULT_OBJECT_GROUP INTEGER,
	CAN_CHANGE_PASSWORD VARCHAR(1), 
	ACTIVE_STATUS    VARCHAR(1), 
	UPDATE_DATE    DATE  
);


-- ART_DATABASES
-- Stores Target Database definitions

CREATE TABLE ART_DATABASES
(
	DATABASE_ID       INTEGER NOT NULL PRIMARY KEY,
	NAME	          VARCHAR(25) NOT NULL,
	DRIVER            VARCHAR(200) NOT NULL,
	URL               VARCHAR(2000) NOT NULL,
	USERNAME          VARCHAR(25) NOT NULL,
	PASSWORD          VARCHAR(40) NOT NULL,
	POOL_TIMEOUT      INTEGER,  
	TEST_SQL          VARCHAR(60),         
	UPDATE_DATE       DATE	
);
-- create unique constraints
ALTER TABLE ART_DATABASES ADD CONSTRAINT art_databases_uc_name UNIQUE (NAME);


-- ART_QUERY_GROUPS
-- Stores name and description of query groups
-- Query Group ID 0 is for LOV queries

CREATE TABLE ART_QUERY_GROUPS
(
	QUERY_GROUP_ID  INTEGER  NOT NULL PRIMARY KEY,  
	NAME            VARCHAR(25) NOT NULL,
	DESCRIPTION     VARCHAR(60)  
);
-- create unique constraints
ALTER TABLE ART_QUERY_GROUPS ADD CONSTRAINT art_query_groups_uc_name UNIQUE (NAME);


-- ART_QUERIES
-- Stores query header definitions 

-- Query types:
-- 0 = normal query, 1-99 = report on column, 100 = update, 101 = crosstab
-- 102 = crosstab html only, 103 = normal query html only, 110 = dashboard, 111 = text object
-- 112 = mondrian cube, 113 = mondrian cube via xmla, 114 = sql server analysis services cube via xmla
-- 115 = jasper report with template query, 116 = jasper report with art query
-- 117 = jxls spreadsheet with template query, 118 = jxls spreadsheet with art query
-- 119 = dynamic lov, 1120 = static lov
-- Query types for graphs:
-- -1 = XY, -2 = Pie , -3 = Horizontal bar, -4 = Vertical bar, -5 = Line
-- -6 = Time series, -7 = Date series, -8 = stacked vertical bar, -9 = stacked horizontal bar
-- -10 = speedometer

CREATE TABLE ART_QUERIES
(
	QUERY_ID    INTEGER NOT NULL PRIMARY KEY,
	QUERY_GROUP_ID  INTEGER NOT NULL,
	NAME              VARCHAR(25) NOT NULL,
	SHORT_DESCRIPTION VARCHAR(254),
	DESCRIPTION       VARCHAR(2000),
	USES_RULES  VARCHAR(1),
	DATABASE_ID	    INTEGER NOT NULL,
	QUERY_TYPE        INTEGER,      	
	CONTACT_PERSON        VARCHAR(20),  
	ACTIVE_STATUS    VARCHAR(1),
	SHOW_PARAMETERS VARCHAR(1),
	X_AXIS_LABEL VARCHAR(50),
	Y_AXIS_LABEL VARCHAR(50),
	GRAPH_OPTIONS VARCHAR(200),
	TEMPLATE VARCHAR(100),
	XMLA_URL VARCHAR(300),
	XMLA_DATASOURCE VARCHAR(50),
	XMLA_CATALOG VARCHAR(50),
	XMLA_USERNAME VARCHAR(50),
	XMLA_PASSWORD VARCHAR(50),
	UPDATE_DATE     DATE	
);
-- create unique constraints
ALTER TABLE ART_QUERIES ADD CONSTRAINT art_queries_uc_name UNIQUE (NAME);


-- ART_ADMIN_PRIVILEGES
-- stores privileges for Junior and Mid Admin (Admin Level <=30)
-- this table is used to limit data extraction for these admins when viewing available groups and databases

-- PRIVILEGE can be either DB (database) or GRP (Group)
-- VALUE_ID is the DATABASE_ID or the QUERY_GROUP_ID

CREATE TABLE ART_ADMIN_PRIVILEGES
(
	USERNAME    VARCHAR(15) NOT NULL,
	PRIVILEGE   VARCHAR(4) NOT NULL,
	VALUE_ID    INTEGER NOT NULL,
	PRIMARY KEY(USERNAME,PRIVILEGE,VALUE_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME)
);


-- ART_USER_QUERIES
-- Stores the queries a user can execute

CREATE TABLE ART_USER_QUERIES
(
	USERNAME    VARCHAR(15) NOT NULL,
	QUERY_ID    INTEGER     NOT NULL,  
	UPDATE_DATE  DATE,  
	PRIMARY KEY(USERNAME,QUERY_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME),
	FOREIGN KEY (QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID)
);


-- ART_USER_QUERY_GROUPS
-- Stores query_groups a user can deal with

CREATE TABLE ART_USER_QUERY_GROUPS
(
	USERNAME       VARCHAR(15) NOT NULL,
	QUERY_GROUP_ID INTEGER     NOT NULL,        
	PRIMARY KEY(USERNAME,QUERY_GROUP_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME),
	FOREIGN KEY (QUERY_GROUP_ID) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID)
);


-- ART_QUERY_FIELDS
-- Stores query parameters

-- FIELD_POSITION is the order the parameter is displayed to users
-- FIELD_CLASS stores the data type of the parameter
-- PARAM_TYPE: M for MULTI param, I for INLINE param (N for obsolete bind parameters)
-- PARAM_LABEL stores the column name for non-labelled MULTI params or the parameter label for INLINE params or labelled multi params
-- USE_LOV is set to Y if the param values are provided by an LOV query
-- CHAINED_PARAM_POSITION is the position of the chained param (in osolete Bind params this is the index of the ? in the prepared statement)
-- CHAINED_VALUE_POSITION - allow chained parameter value to come from a different parameter from the previous one in the chained parameter sequence
-- DRILLDOWN_COLUMN - if used in a drill down report, referes to the column in the parent report on which the parameter will be applied 

CREATE TABLE ART_QUERY_FIELDS
(	
	QUERY_ID                INTEGER     NOT NULL,
	FIELD_POSITION          INTEGER     NOT NULL, 
	NAME                    VARCHAR(25) NOT NULL,
	SHORT_DESCRIPTION       VARCHAR(40),
	DESCRIPTION             VARCHAR(120),
	FIELD_CLASS             VARCHAR(15) NOT NULL,
	DEFAULT_VALUE           VARCHAR(80),
	PARAM_TYPE VARCHAR(1) NOT NULL,           
	PARAM_LABEL     VARCHAR(55),          
	USE_LOV       VARCHAR(1), 		
	APPLY_RULES_TO_LOV        VARCHAR(1),
	LOV_QUERY_ID  INTEGER,
	CHAINED_PARAM_POSITION  INTEGER,              
	CHAINED_VALUE_POSITION INTEGER,
	DRILLDOWN_COLUMN INTEGER,	
	UPDATE_DATE     DATE,	
	PRIMARY KEY (QUERY_ID,FIELD_POSITION),	
	FOREIGN KEY (QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID)
);


-- ART_ALL_SOURCES
-- Stores source code for queries (sql, mdx, xml, html, text)

CREATE TABLE ART_ALL_SOURCES
(
	OBJECT_ID              INTEGER      NOT NULL,
	OBJECT_GROUP_ID        INTEGER      NOT NULL,	
	LINE_NUMBER            INTEGER      NOT NULL,
	TEXT_INFO              VARCHAR(4000) NOT NULL,
	PRIMARY KEY (OBJECT_ID,LINE_NUMBER),
	FOREIGN KEY (OBJECT_ID) REFERENCES ART_QUERIES(QUERY_ID)
);


-- ART_RULES
-- Stores Rule definitions (names)
 
CREATE TABLE ART_RULES
(  
	RULE_NAME         VARCHAR(15) NOT NULL PRIMARY KEY,
	SHORT_DESCRIPTION VARCHAR(40)	
);


-- ART_QUERY_RULES
-- Stores rules-query relationships 

CREATE TABLE ART_QUERY_RULES
(  
	QUERY_ID          INTEGER       NOT NULL,
	FIELD_NAME        VARCHAR(40)   NOT NULL,
	RULE_NAME          VARCHAR(15)   NOT NULL, 
	PRIMARY KEY (QUERY_ID,RULE_NAME),
	FOREIGN KEY (QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID),
	FOREIGN KEY (RULE_NAME) REFERENCES ART_RULES(RULE_NAME)
);


-- ART_USER_RULES
-- Stores rules-users relationships
-- RULE_TYPE can be EXACT or LOOKUP
 
CREATE TABLE ART_USER_RULES
(  
	USERNAME          VARCHAR(15)   NOT NULL,
	RULE_NAME         VARCHAR(15)   NOT NULL, 
	RULE_VALUE        VARCHAR(25)   NOT NULL,
	RULE_TYPE		  VARCHAR(6),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME),
	FOREIGN KEY (RULE_NAME) REFERENCES ART_RULES(RULE_NAME)
);


-- ART_JOBS
-- Stores scheduled jobs

-- OUTPUT: html, pdf, xls etc (viewMode code)
-- JOB_TYPE: 1 = alert, 2 = email with query output as attachment, 3 = publish
-- 4 = just execute (i.e. no output generated), 5 = email with query output inline
-- 6 = conditional email attachment, 7 = conditional inline email, 8 = conditional publish
-- 9 = append to cached table, 10 = delete and insert into cached table
-- MIGRATED_TO_QUARTZ is present to allow seamless migration of jobs when upgrading from ART versions before 1.11 (i.e. before quartz was used as the scheduling engine)

CREATE TABLE ART_JOBS
(
	JOB_ID            INTEGER NOT NULL PRIMARY KEY,
	JOB_NAME VARCHAR(50),
	QUERY_ID	    INTEGER NOT NULL,
	USERNAME          VARCHAR(15) NOT NULL,
	OUTPUT_FORMAT            VARCHAR(15) NOT NULL, 
	JOB_TYPE		    INTEGER NOT NULL,      
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100),               
	JOB_WEEKDAY	    VARCHAR(100),               
	JOB_MONTH		    VARCHAR(100),               
	MAIL_TOS          VARCHAR(254),
	MAIL_FROM         VARCHAR(80),
	SUBJECT	    VARCHAR(254),
	MESSAGE           VARCHAR(4000),
	CACHED_TABLE_NAME VARCHAR(30),	
	START_DATE DATE,
	END_DATE DATE,
	NEXT_RUN_DATE TIMESTAMP NULL,		
	LAST_FILE_NAME    VARCHAR(4000),
	LAST_START_DATE   TIMESTAMP NULL,
	LAST_END_DATE     TIMESTAMP NULL, 
	ACTIVE_STATUS          VARCHAR(1) NOT NULL, 
	ENABLE_AUDIT        VARCHAR(1) NOT NULL,				
	ALLOW_SHARING VARCHAR(1),
	ALLOW_SPLITTING VARCHAR(1),	
	MIGRATED_TO_QUARTZ VARCHAR(1)	
);


-- ART_JOBS_PARAMETERS
-- store jobs parameters

-- PARAM_TYPE: M = multi, I = inline (B for obsolete bind parameters)
-- PARAM_NAME: the html element name of the parameter

CREATE TABLE ART_JOBS_PARAMETERS
(
	JOB_ID        INTEGER NOT NULL,
	PARAM_TYPE	VARCHAR(1) NOT NULL,   
	PARAM_NAME		    VARCHAR(60),
	PARAM_VALUE		    VARCHAR(200),
	FOREIGN KEY (JOB_ID) REFERENCES ART_JOBS(JOB_ID)
);


-- ART_JOBS_AUDIT
-- stores logs of every job execution when job auditing is enabled

-- USERNAME: user for whom the job is run
-- JOB_AUDIT_KEY: unique identifier for a job audit record
-- ACTION: S = job started, E = job ended, X = Error occurred while running job

CREATE TABLE ART_JOBS_AUDIT
(
	JOB_ID            INTEGER NOT NULL,
	USERNAME VARCHAR(15),
	JOB_AUDIT_KEY VARCHAR(100),
	JOB_ACTION   VARCHAR(1),             
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL,
	FOREIGN KEY (JOB_ID) REFERENCES ART_JOBS(JOB_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME)
);

		
-- ART_LOGS
-- Stores log information e.g. logins and object execution

-- LOG_TYPE: login = successful login, loginerr = unsuccessful login attempt
-- object = interactive query execution, upload = template file uploaded when creating query that uses a template file
-- TOTAL_TIME: total execution time in secs, including fetch time and display time
-- FETCH_TIME: time elapsed from when the query is submitted to when the database returns 1st row

CREATE TABLE ART_LOGS
(
	UPDATE_TIME TIMESTAMP NOT NULL,
	USERNAME    VARCHAR(15) NOT NULL,
	LOG_TYPE        VARCHAR(15) NOT NULL, 
	IP          VARCHAR(15), 
	OBJECT_ID   INTEGER,
	TOTAL_TIME  INTEGER, 
	FETCH_TIME  INTEGER, 
	MESSAGE     VARCHAR(4000) 
);


-- ART_SHARED_JOBS
-- Stores users who have been given access to a job's output

CREATE TABLE ART_SHARED_JOBS
(
	JOB_ID INTEGER NOT NULL,
	USERNAME VARCHAR(15) NOT NULL,
	USER_GROUP_ID INTEGER,
	LAST_FILE_NAME VARCHAR(4000),
	LAST_START_DATE TIMESTAMP NULL,
	LAST_END_DATE TIMESTAMP NULL,
	PRIMARY KEY (JOB_ID,USERNAME),
	FOREIGN KEY (JOB_ID) REFERENCES ART_JOBS(JOB_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME)
);


-- ART_JOB_SCHEDULES
-- Stores job schedules to enable re-use of schedules when creating jobs

CREATE TABLE ART_JOB_SCHEDULES
(
	SCHEDULE_NAME VARCHAR(50) NOT NULL PRIMARY KEY,
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100), 
	JOB_MONTH		    VARCHAR(100),   	
	JOB_WEEKDAY	    VARCHAR(100)	
);


-- ART_USER_GROUPS
-- Stores user group definitions

CREATE TABLE ART_USER_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL PRIMARY KEY,
	NAME VARCHAR(30) NOT NULL,
	DESCRIPTION VARCHAR(50),
	DEFAULT_OBJECT_GROUP INTEGER	
);
-- create unique constraints
ALTER TABLE ART_USER_GROUPS ADD CONSTRAINT art_user_groups_uc_name UNIQUE (NAME);


-- ART_USER_GROUP_ASSIGNEMENT
-- Stores details of which users belong to which user groups

CREATE TABLE ART_USER_GROUP_ASSIGNMENT
(
	USERNAME VARCHAR(15) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	PRIMARY KEY (USERNAME,USER_GROUP_ID),
	FOREIGN KEY (USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID),
	FOREIGN KEY (USERNAME) REFERENCES ART_USERS(USERNAME)
);


-- ART_USER_GROUP_QUERIES
-- Stores which queries certain user groups can access (users who are members of the group can access the queries)

CREATE TABLE ART_USER_GROUP_QUERIES
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	PRIMARY KEY (USER_GROUP_ID,QUERY_ID),
	FOREIGN KEY (USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID),
	FOREIGN KEY (QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID)
);


-- ART_USER_GROUP_GROUPS
-- Stores which query groups certain user groups can access (users who are members of the group can access the query groups)

CREATE TABLE ART_USER_GROUP_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_GROUP_ID INTEGER NOT NULL,
	PRIMARY KEY (USER_GROUP_ID,QUERY_GROUP_ID),
	FOREIGN KEY (USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID),
	FOREIGN KEY (QUERY_GROUP_ID) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID)
);


-- ART_USER_GROUP_JOBS
-- Stores which jobs have been shared with certain user groups (users who are members of the group can access the job output)

CREATE TABLE ART_USER_GROUP_JOBS
(
	USER_GROUP_ID INTEGER NOT NULL,
	JOB_ID INTEGER NOT NULL,
	PRIMARY KEY (USER_GROUP_ID,JOB_ID),
	FOREIGN KEY (USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID),
	FOREIGN KEY (JOB_ID) REFERENCES ART_JOBS(JOB_ID)
);


-- ART_DRILLDOWN_QUERIES
-- Stores details of drill down queries

CREATE TABLE ART_DRILLDOWN_QUERIES
(
	QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_POSITION INTEGER NOT NULL,
	DRILLDOWN_TITLE VARCHAR(30),
	DRILLDOWN_TEXT VARCHAR(30),
	OUTPUT_FORMAT VARCHAR(15),
	OPEN_IN_NEW_WINDOW VARCHAR(1),
	PRIMARY KEY (QUERY_ID,DRILLDOWN_QUERY_POSITION),
	FOREIGN KEY (QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID),
	FOREIGN KEY (DRILLDOWN_QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID)
);


--
-- Default Data
--
 
-- test object group
INSERT INTO ART_QUERY_GROUPS (QUERY_GROUP_ID,NAME,DESCRIPTION) VALUES
(1, 'Test' , 'Test Group');
 
-- sample job schedule
INSERT INTO ART_JOB_SCHEDULES (SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY) VALUES
('Last Day of Every Month','0','23','L','*','?');

