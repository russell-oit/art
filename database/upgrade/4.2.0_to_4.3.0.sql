-- Upgrade script from ART 4.2 to ART 4.3

-- CHANGES:
-- update database version
-- add json options column
-- add oauth columns
-- rename query_id column in art_logs table


-- NOTES:
-- for sql server, mysql replace TIMESTAMP with DATETIME

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.3-snapshot';

-- add json options column
ALTER TABLE ART_SETTINGS ADD JSON_OPTIONS CLOB;

-- add oauth columns
ALTER TABLE ART_SMTP_SERVERS ADD USE_GOOGLE_OAUTH_2 INTEGER;
UPDATE ART_SMTP_SERVERS SET USE_GOOGLE_OAUTH_2=0;

ALTER TABLE ART_SMTP_SERVERS ADD OAUTH_CLIENT_ID VARCHAR(200);
ALTER TABLE ART_SMTP_SERVERS ADD OAUTH_CLIENT_SECRET VARCHAR(400);
ALTER TABLE ART_SMTP_SERVERS ADD OAUTH_REFRESH_TOKEN VARCHAR(400);
ALTER TABLE ART_SMTP_SERVERS ADD OAUTH_ACCESS_TOKEN VARCHAR(400);
ALTER TABLE ART_SMTP_SERVERS ADD OAUTH_ACCESS_TOKEN_EXPIRY TIMESTAMP;

-- rename query_id column in art_logs table
ALTER TABLE ART_LOGS ADD ITEM_ID INTEGER;
UPDATE ART_LOGS SET ITEM_ID=QUERY_ID;
ALTER TABLE ART_LOGS DROP COLUMN QUERY_ID;
