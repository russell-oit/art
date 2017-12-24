-- Upgrade script from ART 3.1 to ART 3.2

-- CHANGES:
-- update database version
-- add reference report types
-- add smtp servers table
-- add smtp server id to jobs
-- add job options field
-- create settings table


-- NOTES:
-- for sql server, mysql replace TIMESTAMP with DATETIME

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.2-snapshot';

-- add reference report types
INSERT INTO ART_REPORT_TYPES VALUES (153,'Velocity');
INSERT INTO ART_REPORT_TYPES VALUES (154,'OrgChart: Database');
INSERT INTO ART_REPORT_TYPES VALUES (155,'OrgChart: JSON');
INSERT INTO ART_REPORT_TYPES VALUES (156,'OrgChart: List');
INSERT INTO ART_REPORT_TYPES VALUES (157,'OrgChart: Ajax');

-- add smtp servers table
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
	PASSWORD VARCHAR(100),
	SMTP_FROM VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT art_ss_pk PRIMARY KEY(SMTP_SERVER_ID),
	CONSTRAINT art_ss_name_uq UNIQUE(NAME)
);

-- add smtp server id to jobs
ALTER TABLE ART_JOBS ADD SMTP_SERVER_ID INTEGER;

-- add job options field
ALTER TABLE ART_JOBS ADD JOB_OPTIONS CLOB;

-- create settings table
CREATE TABLE ART_SETTINGS
(
	SMTP_SERVER VARCHAR(100),
	SMTP_PORT INTEGER,
	SMTP_USE_STARTTLS INTEGER,
	USE_SMTP_AUTHENTICATION INTEGER,
	SMTP_USERNAME VARCHAR(100),
	SMTP_PASSWORD VARCHAR(100),
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
	LDAP_BIND_PASSWORD VARCHAR(100),
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
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50)
);
