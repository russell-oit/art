-- Migration script from ART 2.0 to ART 2.1
--
-- Purpose: create/update the tables needed to 
--          . support xmla username and password
--
-- this script will work as is for mysql, oracle, postgresql, hsqldb, sql server
-- ------------------------------------------------


-- change to xmla username and password
ALTER TABLE ART_QUERIES ADD XMLA_USERNAME VARCHAR(50);
ALTER TABLE ART_QUERIES ADD XMLA_PASSWORD VARCHAR(50);
