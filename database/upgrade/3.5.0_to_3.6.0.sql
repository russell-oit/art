-- Upgrade script from ART 3.5 to ART 3.6

-- CHANGES:
-- update database version
-- add pivottable.js saved options field


-- NOTES:
-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING
-- for hsqldb, replace CLOB with LONGVARCHAR

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.6-snapshot';

-- add pivottable.js saved options field
ALTER TABLE ART_QUERIES ADD PIVOTTABLEJS_SAVED_OPTIONS CLOB;
