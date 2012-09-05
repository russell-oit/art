-- Upgrade script from ART 2.3.1 to ART 2.4
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . support cc and bcc fields for email jobs
--
-- ------------------------------------------------


-- update database version setting
UPDATE ART_SETTINGS SET SETTING_VALUE='2.4' WHERE SETTING_NAME='database version';

-- add cc and bcc fields for email jobs
ALTER TABLE ART_JOBS ADD MAIL_CC VARCHAR(254);
ALTER TABLE ART_JOBS ADD MAIL_BCC VARCHAR(254);


