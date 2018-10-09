-- Upgrade script from ART 3.9 to ART 3.10

-- CHANGES:
-- update database version
-- increase length of contact person column


-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.10';

-- increase length of contact person column
ALTER TABLE ART_QUERIES MODIFY CONTACT_PERSON VARCHAR(100);
