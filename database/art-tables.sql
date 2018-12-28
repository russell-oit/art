-- Create the ART database

-- IMPORTANT:
-- after running this script, ALSO RUN the tables_xxx.sql script for your database
-- (found in the quartz directory)


-- NOTES:
-- for sql server, mysql replace TIMESTAMP with DATETIME

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR


-- UPGRADING:
-- if you are upgrading, don't use this script. run the scripts in the
-- upgrade directory. run the scripts one at a time to upgrade to newer versions.
-- e.g. from 2.0 to 2.1, then 2.1 to 2.2 etc.

-- sql reserved words checker - https://www.petefreitag.com/tools/sql_reserved_words_checker/

-- ------------------------------------------------


-- ART_DATABASE_VERSION
-- stores the version of the ART database

CREATE TABLE ART_DATABASE_VERSION
(
	DATABASE_VERSION VARCHAR(50)
);
-- insert database version
INSERT INTO ART_DATABASE_VERSION VALUES('4.1-snapshot');


-- ART_CUSTOM_UPGRADES
-- stores indications of custom upgrades that have been performed

CREATE TABLE ART_CUSTOM_UPGRADES
(
	DATABASE_VERSION VARCHAR(50),
	UPGRADED INTEGER
);


-- ART_USERS 
-- Stores user info

-- ACCESS_LEVEL: 0 = normal user, 5 = normal user who can schedule jobs
-- 10 = junior admin, 30 = mid admin, 40 = standard admin, 80 = senior admin
-- 100 = super admin
-- ACTIVE: boolean. 0=false, 1=true
-- CAN_CHANGE_PASSWORD: boolean
-- PUBLIC_USER: boolean

CREATE TABLE ART_USERS
(
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	PASSWORD VARCHAR(200),
	PASSWORD_ALGORITHM VARCHAR(20),
	FULL_NAME VARCHAR(100),  
	EMAIL VARCHAR(100),
	DESCRIPTION VARCHAR(500),
	ACCESS_LEVEL INTEGER,
	DEFAULT_QUERY_GROUP INTEGER,
	START_QUERY VARCHAR(500),
	CAN_CHANGE_PASSWORD INTEGER, 
	ACTIVE INTEGER,
	PUBLIC_USER INTEGER,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT au_pk PRIMARY KEY(USER_ID),
	CONSTRAINT au_uq_unm UNIQUE(USERNAME)
);


-- ART_ACCESS_LEVELS
-- Reference table for user access levels

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


-- ART_USER_GROUPS
-- Stores user group definitions

CREATE TABLE ART_USER_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL,
	NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	DEFAULT_QUERY_GROUP INTEGER,
	START_QUERY VARCHAR(500),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aug_pk PRIMARY KEY(USER_GROUP_ID),
	CONSTRAINT aug_name_uq UNIQUE(NAME)
);


-- ART_USER_GROUP_ASSIGNEMENT
-- Stores details of which users belong to which user groups

CREATE TABLE ART_USER_GROUP_ASSIGNMENT
(
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT auga_pk PRIMARY KEY(USERNAME, USER_GROUP_ID)	
);


-- ART_DATABASES
-- Stores Target Database definitions

-- ACTIVE: boolean
-- JNDI: boolean

CREATE TABLE ART_DATABASES
(
	DATABASE_ID INTEGER NOT NULL,
	NAME	          VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	DATASOURCE_TYPE VARCHAR(20),
	JNDI INTEGER,
	DRIVER            VARCHAR(200),
	URL               VARCHAR(2000),
	USERNAME          VARCHAR(50),
	PASSWORD          VARCHAR(200),
	PASSWORD_ALGORITHM VARCHAR(20),
	POOL_TIMEOUT      INTEGER,  
	TEST_SQL          VARCHAR(60),
	ACTIVE    INTEGER,
	DATASOURCE_OPTIONS CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ad_pk PRIMARY KEY(DATABASE_ID),
	CONSTRAINT ad_name_uq UNIQUE(NAME)
);


-- ART_QUERIES
-- Stores query definitions 

-- USES_RULES: boolean
-- PARAMETERS_IN_OUTPUT: boolean
-- ACTIVE: boolean
-- HIDDEN: boolean
-- OMIT_TITLE_ROW: boolean
-- LOV_USE_DYNAMIC_DATASOURCE: boolean
-- USE_GROOVY: boolean

CREATE TABLE ART_QUERIES
(
	QUERY_ID    INTEGER NOT NULL,	
	NAME              VARCHAR(50) NOT NULL,
	SHORT_DESCRIPTION VARCHAR(254),
	DESCRIPTION       VARCHAR(2000),
	DEVELOPER_COMMENT VARCHAR(2000),
	QUERY_TYPE        INTEGER,
	GROUP_COLUMN INTEGER,
	QUERY_GROUP_ID  INTEGER NOT NULL,	
	DATASOURCE_ID INTEGER,
	CONTACT_PERSON        VARCHAR(100), 
	USES_RULES  INTEGER,	 
	ACTIVE INTEGER,
	HIDDEN INTEGER,
	REPORT_SOURCE CLOB,
	PARAMETERS_IN_OUTPUT INTEGER,
	X_AXIS_LABEL VARCHAR(50),
	Y_AXIS_LABEL VARCHAR(50),
	GRAPH_OPTIONS VARCHAR(200),
	SECONDARY_CHARTS VARCHAR(100),
	TEMPLATE VARCHAR(100),
	DISPLAY_RESULTSET INTEGER,	
	XMLA_DATASOURCE VARCHAR(50),
	XMLA_CATALOG VARCHAR(50),
	DEFAULT_REPORT_FORMAT VARCHAR(50),
	OMIT_TITLE_ROW INTEGER,
	HIDDEN_COLUMNS VARCHAR(500),
	TOTAL_COLUMNS VARCHAR(500),
	DATE_COLUMN_FORMAT VARCHAR(100),
	NUMBER_COLUMN_FORMAT VARCHAR(50),
	COLUMN_FORMATS VARCHAR(2000),
	LOCALE VARCHAR(50),
	NULL_NUMBER_DISPLAY VARCHAR(50),
	NULL_STRING_DISPLAY VARCHAR(50),
	FETCH_SIZE INTEGER,
	REPORT_OPTIONS CLOB,
	PAGE_ORIENTATION VARCHAR(20),
	LOV_USE_DYNAMIC_DATASOURCE INTEGER,
	OPEN_PASSWORD VARCHAR(200),
	MODIFY_PASSWORD VARCHAR(200),
	ENCRYPTOR_ID INTEGER,
	SOURCE_REPORT_ID INTEGER,
	USE_GROOVY INTEGER,
	PIVOTTABLEJS_SAVED_OPTIONS CLOB,
	GRIDSTACK_SAVED_OPTIONS CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aq_pk PRIMARY KEY(QUERY_ID),
	CONSTRAINT aq_name_uq UNIQUE(NAME)
);


-- ART_REPORT_TYPES
-- Reference table for report types

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
INSERT INTO ART_REPORT_TYPES VALUES (115,'JasperReports: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (116,'JasperReports: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (117,'Jxls: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (118,'Jxls: ART Query');
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
INSERT INTO ART_REPORT_TYPES VALUES (152,'CSV');
INSERT INTO ART_REPORT_TYPES VALUES (153,'Velocity');
INSERT INTO ART_REPORT_TYPES VALUES (154,'OrgChart: Database');
INSERT INTO ART_REPORT_TYPES VALUES (155,'OrgChart: JSON');
INSERT INTO ART_REPORT_TYPES VALUES (156,'OrgChart: List');
INSERT INTO ART_REPORT_TYPES VALUES (157,'OrgChart: Ajax');
INSERT INTO ART_REPORT_TYPES VALUES (158,'ReportEngine');
INSERT INTO ART_REPORT_TYPES VALUES (159,'ReportEngine: File');
INSERT INTO ART_REPORT_TYPES VALUES (160,'Plotly.js');
INSERT INTO ART_REPORT_TYPES VALUES (161,'View');
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


-- ART_QUERY_GROUPS
-- Stores name and description of report groups

CREATE TABLE ART_QUERY_GROUPS
(
	QUERY_GROUP_ID  INTEGER  NOT NULL,  
	NAME            VARCHAR(50) NOT NULL,
	DESCRIPTION     VARCHAR(200),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aqg_pk PRIMARY KEY(QUERY_GROUP_ID),
	CONSTRAINT aqg_name_uq UNIQUE(NAME)	
);


-- ART_REPORT_REPORT_GROUPS
-- Stores details of which reports belong to which report groups

CREATE TABLE ART_REPORT_REPORT_GROUPS
(
	REPORT_ID INTEGER NOT NULL,	
	REPORT_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT arrg_pk PRIMARY KEY(REPORT_ID, REPORT_GROUP_ID)	
);


-- ART_ADMIN_PRIVILEGES
-- stores privileges for Junior and Mid Admin (Admin Level <=30)
-- this table is used to limit data extraction for these admins when
-- viewing available groups and datasources

-- PRIVILEGE can be either "DB" (datasource) or "GRP" (query group)
-- VALUE_ID is the datasource id or query group id

CREATE TABLE ART_ADMIN_PRIVILEGES
(	
	USER_ID INTEGER,
	USERNAME    VARCHAR(50) NOT NULL,
	PRIVILEGE   VARCHAR(4) NOT NULL,
	VALUE_ID    INTEGER NOT NULL,
	CONSTRAINT aap_pk PRIMARY KEY(USERNAME, PRIVILEGE, VALUE_ID)	
);


-- ART_USER_QUERIES
-- Stores the queries a user can execute

CREATE TABLE ART_USER_QUERIES
(
	USER_ID INTEGER,
	USERNAME    VARCHAR(50) NOT NULL,
	QUERY_ID    INTEGER     NOT NULL,	 
	CONSTRAINT auq_pk PRIMARY KEY(USERNAME, QUERY_ID)	
);


-- ART_USER_QUERY_GROUPS
-- Stores query groups a user can deal with

CREATE TABLE ART_USER_QUERY_GROUPS
(
	USER_ID INTEGER,
	USERNAME       VARCHAR(50) NOT NULL,
	QUERY_GROUP_ID INTEGER     NOT NULL,        
	CONSTRAINT auqg_pk PRIMARY KEY(USERNAME, QUERY_GROUP_ID)	
);


-- ART_PARAMETERS
-- Stores parameter definitions, holding core parameter attributes

-- HIDDEN: boolean
-- FIXED_VALUE: boolean
-- SHARED: boolean
-- USE_LOV: boolean
-- USE_RULES_IN_LOV: boolean
-- USE_DIRECT_SUBSTITUTION: boolean
-- DRILLDOWN_COLUMN_INDEX - if used in a drilldown report, refers to the column in
-- the parent report on which the parameter will be applied (index starts from 1)
-- USE_DEFAULT_VALUE_IN_JOBS: boolean

CREATE TABLE ART_PARAMETERS
(	
	PARAMETER_ID INTEGER NOT NULL,		
	NAME  VARCHAR(60),
	DESCRIPTION VARCHAR(200),
	PARAMETER_TYPE VARCHAR(30),           
	PARAMETER_LABEL     VARCHAR(50),
	HELP_TEXT            VARCHAR(500),
	DATA_TYPE         VARCHAR(30),
	DEFAULT_VALUE     VARCHAR(4000),
	DEFAULT_VALUE_REPORT_ID INTEGER,
	HIDDEN INTEGER,
	FIXED_VALUE INTEGER,
	SHARED INTEGER,
	USE_LOV INTEGER, 
	LOV_REPORT_ID  INTEGER,
	USE_RULES_IN_LOV INTEGER,
	DRILLDOWN_COLUMN_INDEX INTEGER,
	USE_DIRECT_SUBSTITUTION INTEGER,
	PARAMETER_OPTIONS CLOB,
	PARAMETER_DATE_FORMAT VARCHAR(100),
	PLACEHOLDER_TEXT VARCHAR(100),
	USE_DEFAULT_VALUE_IN_JOBS INTEGER,
	TEMPLATE VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ap_pk PRIMARY KEY(PARAMETER_ID)	
);


-- ART_REPORT_PARAMETERS
-- Stores parameters used in reports, holding additional parameter attributes

CREATE TABLE ART_REPORT_PARAMETERS
(	
	REPORT_PARAMETER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,	
	PARAMETER_ID INTEGER NOT NULL,	
	PARAMETER_POSITION INTEGER NOT NULL,
	CHAINED_PARENTS  VARCHAR(200),              
	CHAINED_DEPENDS VARCHAR(200),
	CONSTRAINT arp_pk PRIMARY KEY(REPORT_PARAMETER_ID)	
);


-- ART_QUERY_FIELDS
-- Stores query parameters

-- FIELD_POSITION is the order the parameter is displayed to users
-- FIELD_CLASS stores the data type of the parameter
-- PARAM_TYPE: M for MULTI param, I for INLINE param 
-- PARAM_LABEL stores the column name for non-labelled MULTI params
-- or the parameter label for INLINE params or labelled multi params
-- USE_LOV is set to Y if the param values are provided by an LOV query
-- CHAINED_PARAM_POSITION is the position of the chained param 
-- CHAINED_VALUE_POSITION - allow chained parameter value to come from
-- a different parameter from the previous one in the chained parameter sequence
-- DRILLDOWN_COLUMN - if used in a drill down report, refers to the column in
-- the parent report on which the parameter will be applied 

CREATE TABLE ART_QUERY_FIELDS
(	
	QUERY_ID                INTEGER     NOT NULL,
	FIELD_POSITION          INTEGER     NOT NULL, 
	NAME                    VARCHAR(25),
	SHORT_DESCRIPTION       VARCHAR(40),
	DESCRIPTION             VARCHAR(120),
	PARAM_TYPE VARCHAR(1) NOT NULL,           
	PARAM_LABEL     VARCHAR(55),  
	PARAM_DATA_TYPE         VARCHAR(15) NOT NULL,
	DEFAULT_VALUE           VARCHAR(80),	        
	USE_LOV       VARCHAR(1), 		
	APPLY_RULES_TO_LOV        VARCHAR(1),
	LOV_QUERY_ID  INTEGER,
	CHAINED_PARAM_POSITION  INTEGER,              
	CHAINED_VALUE_POSITION INTEGER,
	DRILLDOWN_COLUMN INTEGER,
	DIRECT_SUBSTITUTION VARCHAR(1),
	MIGRATED INTEGER,
	UPDATE_DATE TIMESTAMP,	
	CONSTRAINT aqf_pk PRIMARY KEY(QUERY_ID, FIELD_POSITION)	
);


-- ART_ALL_SOURCES
-- Stores source code for queries (sql, mdx, xml, html, text)

CREATE TABLE ART_ALL_SOURCES
(
	OBJECT_ID              INTEGER      NOT NULL,	
	LINE_NUMBER            INTEGER      NOT NULL,
	SOURCE_INFO              VARCHAR(4000),
	CONSTRAINT aas_pk PRIMARY KEY(OBJECT_ID, LINE_NUMBER)	
);


-- ART_RULES
-- Stores Rule definitions
 
CREATE TABLE ART_RULES
(
	RULE_ID INTEGER NOT NULL,
	RULE_NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	DATA_TYPE VARCHAR(30),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ar_pk PRIMARY KEY(RULE_ID),
	CONSTRAINT ar_rname_uq UNIQUE(RULE_NAME)
);


-- ART_QUERY_RULES
-- Stores rules-query relationships 

CREATE TABLE ART_QUERY_RULES
(
	QUERY_RULE_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL,
	FIELD_NAME VARCHAR(100),
	FIELD_DATA_TYPE VARCHAR(15), 
	CONSTRAINT aqr_pk PRIMARY KEY(QUERY_ID, RULE_NAME)	
);


-- ART_USER_RULES
-- Stores rule values for users
-- RULE_TYPE can be EXACT or LOOKUP
 
CREATE TABLE ART_USER_RULES
(  
	RULE_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL, 
	RULE_VALUE VARCHAR(100) NOT NULL,
	RULE_TYPE VARCHAR(6)	
);


-- ART_USER_GROUP_RULES
-- Stores rule values for user groups
-- RULE_TYPE can be EXACT or LOOKUP
 
CREATE TABLE ART_USER_GROUP_RULES
(  
	RULE_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL, 
	RULE_VALUE VARCHAR(100) NOT NULL,
	RULE_TYPE VARCHAR(6)	
);


-- ART_JOBS
-- Stores scheduled jobs

-- OUTPUT_FORMAT: html, pdf, xls etc
-- LAST_FILE_NAME: Contains result of last job execution
-- MIGRATED_TO_QUARTZ is present to allow seamless migration of jobs when
-- upgrading from ART versions before 1.11 (before quartz was used as the scheduling engine)
-- ACTIVE: boolean. 0=false, 1=true

CREATE TABLE ART_JOBS
(
	JOB_ID INTEGER NOT NULL,
	JOB_NAME VARCHAR(50),
	QUERY_ID	    INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME          VARCHAR(50) NOT NULL,
	OUTPUT_FORMAT            VARCHAR(50) NOT NULL, 
	JOB_TYPE VARCHAR(50),
	JOB_SECOND VARCHAR(100),
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100), 
	JOB_MONTH		    VARCHAR(100),               	
	JOB_WEEKDAY	    VARCHAR(100),
	JOB_YEAR VARCHAR(100),
	TIME_ZONE VARCHAR(50),
	MAIL_TOS          VARCHAR(254),
	MAIL_FROM         VARCHAR(80),
	MAIL_CC VARCHAR(254),
	MAIL_BCC VARCHAR(254),
	SUBJECT	    VARCHAR(1000),
	MESSAGE CLOB,
	CACHED_DATASOURCE_ID INTEGER,
	CACHED_TABLE_NAME VARCHAR(30),	
	START_DATE TIMESTAMP,
	END_DATE TIMESTAMP,
	NEXT_RUN_DATE TIMESTAMP NULL,		
	LAST_FILE_NAME    VARCHAR(200),
	LAST_RUN_MESSAGE VARCHAR(100),
	LAST_RUN_DETAILS    VARCHAR(4000),
	LAST_START_DATE   TIMESTAMP NULL,
	LAST_END_DATE     TIMESTAMP NULL,
	ACTIVE INTEGER,
	ENABLE_AUDIT INTEGER,				
	ALLOW_SHARING INTEGER,
	ALLOW_SPLITTING INTEGER,
	RECIPIENTS_QUERY_ID INTEGER,
	RUNS_TO_ARCHIVE INTEGER,
	MIGRATED_TO_QUARTZ VARCHAR(1),
	FIXED_FILE_NAME VARCHAR(1000),
	SUB_DIRECTORY VARCHAR(100),
	BATCH_FILE VARCHAR(50),
	FTP_SERVER_ID INTEGER,
	EMAIL_TEMPLATE VARCHAR(100),
	EXTRA_SCHEDULES CLOB,
	HOLIDAYS CLOB,
	QUARTZ_CALENDAR_NAMES VARCHAR(100),
	SCHEDULE_ID INTEGER,
	SMTP_SERVER_ID INTEGER,
	JOB_OPTIONS CLOB,
	ERROR_EMAIL_TO VARCHAR(500),
	PRE_RUN_REPORT VARCHAR(50),
	POST_RUN_REPORT VARCHAR(50),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aj_pk PRIMARY KEY(JOB_ID)
);


-- ART_JOBS_PARAMETERS
-- store jobs parameters

-- PARAM_TYPE: M = multi, I = inline 
-- PARAM_NAME: the html element name of the parameter

CREATE TABLE ART_JOBS_PARAMETERS
(
	JOB_ID        INTEGER NOT NULL,
	PARAM_TYPE	VARCHAR(1) NOT NULL,   
	PARAM_NAME		    VARCHAR(60),
	PARAM_VALUE		    VARCHAR(4000)	
);


-- ART_JOBS_AUDIT
-- stores logs of every job execution when job auditing is enabled

-- USERNAME: user for whom the job is run
-- JOB_AUDIT_KEY: unique identifier for a job audit record
-- ACTION: S = job started, E = job ended, X = Error occurred while running job

CREATE TABLE ART_JOBS_AUDIT
(
	JOB_ID            INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50),
	JOB_AUDIT_KEY VARCHAR(100),
	JOB_ACTION   VARCHAR(1),             
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL	
);

		
-- ART_LOGS
-- Stores log information e.g. logins and report execution

-- LOG_TYPE: login = successful login, loginerr = unsuccessful login attempt
-- report = interactive report execution, upload = template file uploaded when
-- creating query that uses a template file
-- TOTAL_TIME: total execution time in secs, including fetch time and display time
-- FETCH_TIME: time elapsed from when the query is submitted to when the
-- database returns 1st row

CREATE TABLE ART_LOGS
(
	LOG_DATE TIMESTAMP NOT NULL,	
	USERNAME VARCHAR(50) NOT NULL,
	LOG_TYPE VARCHAR(50) NOT NULL, 
	IP VARCHAR(50), 
	QUERY_ID INTEGER,
	TOTAL_TIME INTEGER, 
	FETCH_TIME INTEGER, 
	MESSAGE VARCHAR(500) 
);


-- ART_USER_JOBS
-- Stores users who have been given access to a job's output

-- USER_GROUP_ID: used to indicate if job was shared via user group. To enable
-- deletion of split job records where access was granted via user group,
-- when a user is removed from a group.
-- LAST_FILE_NAME: contains file name for individualized output (split job),
-- or NULL if file name to use comes from ART_JOBS table

CREATE TABLE ART_USER_JOBS
(
	JOB_ID INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER,
	LAST_FILE_NAME VARCHAR(4000),
	LAST_RUN_MESSAGE VARCHAR(100),
	LAST_RUN_DETAILS VARCHAR(4000),
	LAST_START_DATE TIMESTAMP NULL,
	LAST_END_DATE TIMESTAMP NULL,
	CONSTRAINT auj_pk PRIMARY KEY(JOB_ID, USERNAME)	
);


-- ART_JOB_SCHEDULES
-- Stores job schedules to enable re-use of schedules when creating jobs

CREATE TABLE ART_JOB_SCHEDULES
(
	SCHEDULE_ID INTEGER NOT NULL,
	SCHEDULE_NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	JOB_SECOND VARCHAR(100),
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100), 
	JOB_MONTH		    VARCHAR(100),   	
	JOB_WEEKDAY	    VARCHAR(100),
	JOB_YEAR VARCHAR(100),
	TIME_ZONE VARCHAR(50),
	EXTRA_SCHEDULES CLOB,
	HOLIDAYS CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ajs_pk PRIMARY KEY(SCHEDULE_ID),
	CONSTRAINT ajs_sname_uq UNIQUE(SCHEDULE_NAME)
);


-- ART_USER_GROUP_QUERIES
-- Stores which queries certain user groups can access (users who are members of 
-- the group can access the queries)

CREATE TABLE ART_USER_GROUP_QUERIES
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	CONSTRAINT augq_pk PRIMARY KEY(USER_GROUP_ID, QUERY_ID)	
);


-- ART_USER_GROUP_GROUPS
-- Stores which query groups certain user groups can access (users who are members
-- of the group can access the query groups)

CREATE TABLE ART_USER_GROUP_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT augg_pk PRIMARY KEY(USER_GROUP_ID, QUERY_GROUP_ID)	
);


-- ART_USER_GROUP_JOBS
-- Stores which jobs have been shared with certain user groups (users who are
-- members of the group can access the job output)

CREATE TABLE ART_USER_GROUP_JOBS
(
	USER_GROUP_ID INTEGER NOT NULL,
	JOB_ID INTEGER NOT NULL,
	CONSTRAINT augj_pk PRIMARY KEY(USER_GROUP_ID, JOB_ID)	
);


-- ART_DRILLDOWN_QUERIES
-- Stores details of drill down queries

-- OPEN_IN_NEW_WINDOW: boolean

CREATE TABLE ART_DRILLDOWN_QUERIES
(
	DRILLDOWN_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_POSITION INTEGER NOT NULL,
	DRILLDOWN_TITLE VARCHAR(50),
	DRILLDOWN_TEXT VARCHAR(50),
	OUTPUT_FORMAT VARCHAR(50),
	OPEN_IN_NEW_WINDOW INTEGER,
	CONSTRAINT adq_pk PRIMARY KEY(QUERY_ID, DRILLDOWN_QUERY_POSITION)	
);


-- ART_JOB_ARCHIVES
-- Stored details of past runs for publish jobs

-- JOB_SHARED: N = job not shared, Y = job shared, S = split job

CREATE TABLE ART_JOB_ARCHIVES
(
	ARCHIVE_ID VARCHAR(100) NOT NULL,
	JOB_ID INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50) NOT NULL,	
	ARCHIVE_FILE_NAME VARCHAR(4000),
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL,
	JOB_SHARED VARCHAR(1),
	CONSTRAINT aja_pk PRIMARY KEY(ARCHIVE_ID)
);


-- ART_LOGGED_IN_USERS
-- Stores approximate indication of the currently logged in users

CREATE TABLE ART_LOGGED_IN_USERS
(
	LOGGED_IN_USERS_ID VARCHAR(100) NOT NULL,
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	LOGIN_DATE TIMESTAMP NULL,
	IP_ADDRESS VARCHAR(50),
	CONSTRAINT alu_pk PRIMARY KEY(LOGGED_IN_USERS_ID)
);


-- ART_FTP_SERVERS
-- Stores configurations for ftp servers

-- ACTIVE: boolean

CREATE TABLE ART_FTP_SERVERS
(
	FTP_SERVER_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	CONNECTION_TYPE VARCHAR(20),
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

-- ART_ENCRYPTORS
-- Stores configurations for file encryptors

-- ACTIVE: boolean

CREATE TABLE ART_ENCRYPTORS
(
	ENCRYPTOR_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	ENCRYPTOR_TYPE VARCHAR(50),
	AESCRYPT_PASSWORD VARCHAR(200),
	OPENPGP_PUBLIC_KEY_FILE VARCHAR(100),
	OPENPGP_PUBLIC_KEY_STRING CLOB,
	OPENPGP_SIGNING_KEY_FILE VARCHAR(100),
	OPENPGP_SIGNING_KEY_PASSPHRASE VARCHAR(1000),
	OPEN_PASSWORD VARCHAR(200),
	MODIFY_PASSWORD VARCHAR(200),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ae_pk PRIMARY KEY(ENCRYPTOR_ID),
	CONSTRAINT ae_name_uq UNIQUE(NAME)
);


-- ART_HOLIDAYS
-- Stores configuration of schedule holidays

CREATE TABLE ART_HOLIDAYS
(
	HOLIDAY_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	HOLIDAY_DEFINITION CLOB,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ah_pk PRIMARY KEY(HOLIDAY_ID),
	CONSTRAINT ah_name_uq UNIQUE(NAME)
);


-- ART_SCHEDULE_HOLIDAY_MAP
-- Stores schedule-holiday records

CREATE TABLE ART_SCHEDULE_HOLIDAY_MAP
(
	SCHEDULE_ID INTEGER NOT NULL,
	HOLIDAY_ID INTEGER NOT NULL,
	CONSTRAINT ashm_pk PRIMARY KEY(SCHEDULE_ID, HOLIDAY_ID)
);


-- ART_JOB_HOLIDAY_MAP
-- Stores job-holiday records

CREATE TABLE ART_JOB_HOLIDAY_MAP
(
	JOB_ID INTEGER NOT NULL,
	HOLIDAY_ID INTEGER NOT NULL,
	CONSTRAINT ajhm_pk PRIMARY KEY(JOB_ID, HOLIDAY_ID)
);


-- ART_DESTINATIONS
-- Stores job destination details

-- ACTIVE: boolean
-- CREATE_DIRECTORIES: boolean

CREATE TABLE ART_DESTINATIONS
(
	DESTINATION_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	DESTINATION_TYPE VARCHAR(50),
	SERVER VARCHAR(100),
	PORT INTEGER,
	DESTINATION_USER VARCHAR(50),
	DESTINATION_PASSWORD VARCHAR(200),
	USER_DOMAIN VARCHAR(100),
	DESTINATION_PATH VARCHAR(1000),
	SUB_DIRECTORY VARCHAR(100),
	CREATE_DIRECTORIES INTEGER,
	DESTINATION_OPTIONS CLOB,
	GOOGLE_JSON_KEY_FILE VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ad_pk PRIMARY KEY(DESTINATION_ID),
	CONSTRAINT ad_name_uq UNIQUE(NAME)
);


-- ART_JOB_DESTINATION_MAP
-- Stores job-destination records

CREATE TABLE ART_JOB_DESTINATION_MAP
(
	JOB_ID INTEGER NOT NULL,
	DESTINATION_ID INTEGER NOT NULL,
	CONSTRAINT ajdm_pk PRIMARY KEY(JOB_ID, DESTINATION_ID)
);

-- ART_SMTP_SERVERS
-- Stores configurations for smtp servers

-- ACTIVE: boolean
-- USE_STARTTLS: boolean
-- USE_SMTP_AUTHENTICATION: boolean

CREATE TABLE ART_SMTP_SERVERS
(
	SMTP_SERVER_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,	
	SERVER VARCHAR(100),
	PORT INTEGER,
	USE_STARTTLS INTEGER,
	USE_SMTP_AUTHENTICATION INTEGER,
	USERNAME VARCHAR(100),
	PASSWORD VARCHAR(200),
	SMTP_FROM VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_ss_pk PRIMARY KEY(SMTP_SERVER_ID),
	CONSTRAINT art_ss_name_uq UNIQUE(NAME)
);


-- ART_SETTINGS
-- Stores application settings

-- SMTP_USE_STARTTLS: boolean
-- USE_SMTP_AUTHENTICATION: boolean
-- USE_LDAP_ANONYMOUS_BIND: boolean
-- PDF_FONT_EMBEDDED: boolean
-- HEADER_IN_PUBLIC_SESSION: boolean
-- SCHEDULING_ENABLED: boolean
-- DIRECT_REPORT_EMAILING: boolean

CREATE TABLE ART_SETTINGS
(
	SMTP_SERVER VARCHAR(100),
	SMTP_PORT INTEGER,
	SMTP_USE_STARTTLS INTEGER,
	USE_SMTP_AUTHENTICATION INTEGER,
	SMTP_USERNAME VARCHAR(100),
	SMTP_PASSWORD VARCHAR(200),
	SMTP_FROM VARCHAR(100),
	ART_AUTHENTICATION_METHOD VARCHAR(50),
	WINDOWS_DOMAIN_CONTROLLER VARCHAR(100),
	ALLOWED_WINDOWS_DOMAINS VARCHAR(200),
	DB_AUTHENTICATION_DRIVER VARCHAR(100),
	DB_AUTHENTICATION_URL VARCHAR(500),
	LDAP_SERVER VARCHAR(100),
	LDAP_PORT INTEGER,
	LDAP_ENCRYPTION_METHOD VARCHAR(50),
	LDAP_URL VARCHAR(500),
	LDAP_BASE_DN VARCHAR(500),
	USE_LDAP_ANONYMOUS_BIND INTEGER,
	LDAP_BIND_DN VARCHAR(500),
	LDAP_BIND_PASSWORD VARCHAR(200),
	LDAP_USER_ID_ATTRIBUTE VARCHAR(50),
	LDAP_AUTHENTICATION_METHOD VARCHAR(50),
	LDAP_REALM VARCHAR(200),
	CAS_LOGOUT_URL VARCHAR(100),
	MAX_ROWS_DEFAULT INTEGER,
	MAX_ROWS_SPECIFIC VARCHAR(500),
	PDF_FONT_NAME VARCHAR(50),
	PDF_FONT_FILE VARCHAR(500),
	PDF_FONT_DIRECTORY VARCHAR(500),
	PDF_FONT_ENCODING VARCHAR(50),
	PDF_FONT_EMBEDDED INTEGER,
	ADMIN_EMAIL VARCHAR(100),
	APP_DATE_FORMAT VARCHAR(50),
	APP_TIME_FORMAT VARCHAR(50),
	REPORT_FORMATS VARCHAR(200),
	MAX_RUNNING_REPORTS INTEGER,
	HEADER_IN_PUBLIC_SESSION INTEGER,
	MONDRIAN_CACHE_EXPIRY INTEGER,
	SCHEDULING_ENABLED INTEGER,
	RSS_LINK VARCHAR(500),
	MAX_FILE_UPLOAD_SIZE INTEGER,
	ART_BASE_URL VARCHAR(500),
	SYSTEM_LOCALE VARCHAR(50),
	LOGS_DATASOURCE_ID INTEGER,
	ERROR_EMAIL_TO VARCHAR(500),
	ERROR_EMAIL_FROM VARCHAR(100),
	ERROR_EMAIL_SUBJECT_PATTERN VARCHAR(50),
	ERROR_EMAIL_LEVEL VARCHAR(10),
	ERROR_EMAIL_LOGGER VARCHAR(200),
	ERROR_EMAIL_SUPPRESS_AFTER VARCHAR(30),
	ERROR_EMAIL_EXPIRE_AFTER VARCHAR(20),
	ERROR_EMAIL_DIGEST_FREQUENCY VARCHAR(20),
	PASSWORD_MIN_LENGTH INTEGER,
	PASSWORD_MIN_LOWERCASE INTEGER,
	PASSWORD_MIN_UPPERCASE INTEGER,
	PASSWORD_MIN_NUMERIC INTEGER,
	PASSWORD_MIN_SPECIAL INTEGER,
	JWT_TOKEN_EXPIRY INTEGER,
	DIRECT_REPORT_EMAILING INTEGER,
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50)
);


-- ART_SAVED_PARAMETERS
-- Stores report parameter selections for users

CREATE TABLE ART_SAVED_PARAMETERS
(
	USER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,
	PARAM_NAME VARCHAR(60) NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);


-- ART_USER_PARAM_DEFAULTS
-- Stores user parameter default values

CREATE TABLE ART_USER_PARAM_DEFAULTS
(
	PARAM_DEFAULT_KEY VARCHAR(50) NOT NULL,
	USER_ID INTEGER NOT NULL,
	PARAMETER_ID INTEGER NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);


-- ART_USER_GROUP_PARAM_DEFAULTS
-- Stores user group parameter default values

CREATE TABLE ART_USER_GROUP_PARAM_DEFAULTS
(
	PARAM_DEFAULT_KEY VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	PARAMETER_ID INTEGER NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);


-- ART_USER_FIXED_PARAM_VAL
-- Stores user fixed parameter values

CREATE TABLE ART_USER_FIXED_PARAM_VAL
(
	FIXED_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_ID INTEGER NOT NULL,
	PARAMETER_ID INTEGER NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);


-- ART_USER_GROUP_FIXED_PARAM_VAL
-- Stores user group fixed parameter values

CREATE TABLE ART_USER_GROUP_FIXED_PARAM_VAL
(
	FIXED_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	PARAMETER_ID INTEGER NOT NULL,
	PARAM_VALUE VARCHAR(4000)
);


-- ART_ROLES
-- Stores roles

CREATE TABLE ART_ROLES
(
	ROLE_ID INTEGER NOT NULL,
	NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_rls_pk PRIMARY KEY(ROLE_ID),
	CONSTRAINT art_rls_uq_nm UNIQUE(NAME)
);

-- insert default roles
INSERT INTO ART_ROLES (ROLE_ID, NAME) VALUES(1, 'Admin');
INSERT INTO ART_ROLES (ROLE_ID, NAME) VALUES(2, 'User');


-- ART_PERMISSIONS
-- Stores permissions

CREATE TABLE ART_PERMISSIONS
(
	PERMISSION_ID INTEGER NOT NULL,
	NAME VARCHAR(100) NOT NULL,
	CONSTRAINT art_pm_pk PRIMARY KEY(PERMISSION_ID),
	CONSTRAINT art_pm_uq_nm UNIQUE(NAME)
);

-- insert permissions
INSERT INTO ART_PERMISSIONS VALUES(1, 'view_reports');
INSERT INTO ART_PERMISSIONS VALUES(2, 'save_reports');
INSERT INTO ART_PERMISSIONS VALUES(3, 'self_service_dashboards');
INSERT INTO ART_PERMISSIONS VALUES(4, 'schedule_jobs');
INSERT INTO ART_PERMISSIONS VALUES(5, 'view_jobs');
INSERT INTO ART_PERMISSIONS VALUES(6, 'configure_jobs');
INSERT INTO ART_PERMISSIONS VALUES(7, 'view_logs');
INSERT INTO ART_PERMISSIONS VALUES(8, 'configure_users');
INSERT INTO ART_PERMISSIONS VALUES(9, 'configure_art_database');
INSERT INTO ART_PERMISSIONS VALUES(10, 'configure_settings');
INSERT INTO ART_PERMISSIONS VALUES(11, 'configure_user_groups');
INSERT INTO ART_PERMISSIONS VALUES(12, 'configure_datasources');
INSERT INTO ART_PERMISSIONS VALUES(13, 'configure_reports');
INSERT INTO ART_PERMISSIONS VALUES(14, 'configure_caches');
INSERT INTO ART_PERMISSIONS VALUES(15, 'configure_connections');
INSERT INTO ART_PERMISSIONS VALUES(16, 'configure_loggers');
INSERT INTO ART_PERMISSIONS VALUES(17, 'configure_report_groups');
INSERT INTO ART_PERMISSIONS VALUES(18, 'configure_schedules');
INSERT INTO ART_PERMISSIONS VALUES(19, 'configure_holidays');
INSERT INTO ART_PERMISSIONS VALUES(20, 'configure_destinations');
INSERT INTO ART_PERMISSIONS VALUES(21, 'configure_admin_rights');
INSERT INTO ART_PERMISSIONS VALUES(22, 'configure_access_rights');
INSERT INTO ART_PERMISSIONS VALUES(23, 'configure_user_group_membership');
INSERT INTO ART_PERMISSIONS VALUES(24, 'configure_report_group_membership');
INSERT INTO ART_PERMISSIONS VALUES(25, 'configure_smtp_servers');
INSERT INTO ART_PERMISSIONS VALUES(26, 'configure_encryptors');
INSERT INTO ART_PERMISSIONS VALUES(27, 'migrate_records');
INSERT INTO ART_PERMISSIONS VALUES(28, 'configure_roles');
INSERT INTO ART_PERMISSIONS VALUES(29, 'configure_permissions');
INSERT INTO ART_PERMISSIONS VALUES(30, 'self_service_reports');


-- ART_ROLE_PERMISSION_MAP
-- Stores role - permission mappings

CREATE TABLE ART_ROLE_PERMISSION_MAP
(
	ROLE_ID INTEGER NOT NULL,
	PERMISSION_ID INTEGER NOT NULL,
	CONSTRAINT art_rpm_pk PRIMARY KEY(ROLE_ID, PERMISSION_ID)
);

-- insert default role permissions
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 1);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 2);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 3);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 4);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 5);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 6);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 7);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 8);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 9);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 10);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 11);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 12);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 13);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 14);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 15);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 16);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 17);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 18);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 19);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 20);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 21);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 22);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 23);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 24);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 25);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 26);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 27);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 28);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 29);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(1, 30);
INSERT INTO ART_ROLE_PERMISSION_MAP VALUES(2, 1);


-- ART_USER_ROLE_MAP
-- Stores user - role mappings

CREATE TABLE ART_USER_ROLE_MAP
(
	USER_ID INTEGER NOT NULL,
	ROLE_ID INTEGER NOT NULL,
	CONSTRAINT art_urm_pk PRIMARY KEY(USER_ID, ROLE_ID)
);


-- ART_USER_GROUP_ROLE_MAP
-- Stores user group - role mappings

CREATE TABLE ART_USER_GROUP_ROLE_MAP
(
	USER_GROUP_ID INTEGER NOT NULL,
	ROLE_ID INTEGER NOT NULL,
	CONSTRAINT art_ugrm_pk PRIMARY KEY(USER_GROUP_ID, ROLE_ID)
);


-- ART_USER_PERMISSION_MAP
-- Stores user - permission mappings

CREATE TABLE ART_USER_PERMISSION_MAP
(
	USER_ID INTEGER NOT NULL,
	PERMISSION_ID INTEGER NOT NULL,
	CONSTRAINT art_upm_pk PRIMARY KEY(USER_ID, PERMISSION_ID)
);


-- ART_USER_GROUP_PERM_MAP
-- Stores user group - permission mappings

CREATE TABLE ART_USER_GROUP_PERM_MAP
(
	USER_GROUP_ID INTEGER NOT NULL,
	PERMISSION_ID INTEGER NOT NULL,
	CONSTRAINT art_ugpm_pk PRIMARY KEY(USER_GROUP_ID, PERMISSION_ID)
);
