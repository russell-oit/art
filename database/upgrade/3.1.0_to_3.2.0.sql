-- Upgrade script from ART 3.1 to ART 3.2

-- CHANGES:
-- update database version
-- add reference report types
-- add smtp servers table
-- add smtp server id to jobs
-- add job options field


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
	SMTP_USER VARCHAR(100),
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