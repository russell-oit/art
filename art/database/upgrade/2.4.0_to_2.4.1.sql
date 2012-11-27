-- Upgrade script from ART 2.4 to ART 2.4.1
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . support direct substitution for inline parameters
--
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='2.4.1' WHERE SETTING_NAME='database version';

-- support direct substitution for inline parameters
ALTER TABLE ART_QUERY_FIELDS ADD DIRECT_SUBSTITUTION VARCHAR(1);


