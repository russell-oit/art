-- Upgrade script from ART 2.3 to ART 2.3.1
--
-- Purpose: create/update the tables needed to 
--          . update database version
--
-- ------------------------------------------------


-- update database version setting
UPDATE ART_SETTINGS SET SETTING_VALUE='2.3.1' WHERE SETTING_NAME='database version';


