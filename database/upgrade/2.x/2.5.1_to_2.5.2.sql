-- Upgrade script from ART 2.5.1 to ART 2.5.2
--
-- Purpose: create/update the tables needed to 
--          . update database version
--
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='2.5.2' WHERE SETTING_NAME='database version';





