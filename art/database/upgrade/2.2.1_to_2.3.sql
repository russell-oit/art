-- Upgrade script from ART 2.2.1 to ART 2.3
--
-- Purpose: create/update the tables needed to 
--          . update database version
--
-- ------------------------------------------------


-- update database version setting
UPDATE ART_SETTINGS SET SETTING_VALUE='2.3' WHERE SETTING_NAME='database version';

