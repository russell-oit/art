-- Upgrade script from ART 4.2 to ART 4.3

-- CHANGES:
-- update database version
-- add json options column


-- NOTES:
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
