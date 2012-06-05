-- Upgrade script from ART 2.2.1 to ART 2.3
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . update quartz from 1.8.x to 2.x
--
-- ------------------------------------------------


-- ***************
-- IMPORTANT!!!

-- After running this upgrade script, run the quartz script for your database (located in the quartz directory)
-- This is not the usual process for upgrades, but is necessary for this upgrade as the
-- quartz database schema has changed between 1.8.x and 2.x

-- *****************


-- update database version setting
UPDATE ART_SETTINGS SET SETTING_VALUE='2.3' WHERE SETTING_NAME='database version';

-- delete quartz 1.8.x tables
DROP TABLE QRTZ_JOB_LISTENERS;
DROP TABLE QRTZ_TRIGGER_LISTENERS;
DROP TABLE QRTZ_FIRED_TRIGGERS;
DROP TABLE QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE QRTZ_SCHEDULER_STATE;
DROP TABLE QRTZ_LOCKS;
DROP TABLE QRTZ_SIMPLE_TRIGGERS;
DROP TABLE QRTZ_CRON_TRIGGERS;
DROP TABLE QRTZ_BLOB_TRIGGERS;
DROP TABLE QRTZ_TRIGGERS;
DROP TABLE QRTZ_JOB_DETAILS;
DROP TABLE QRTZ_CALENDARS;

-- update job migration to quartz status so that all jobs are recreated in the new quartz tables
UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ='N';

