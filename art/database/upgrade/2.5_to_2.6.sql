-- Upgrade script from ART 2.5 to ART 2.6
--
-- Purpose: create/update the tables needed to 
--          . update database version
--
-- NOTE:
-- Ensure the NAME column of the ART_QUERIES table is set to VARCHAR(50)
-- Enlarge it manually if it is not
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='2.6-alpha1' WHERE SETTING_NAME='database version';





