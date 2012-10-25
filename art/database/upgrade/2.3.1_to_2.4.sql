-- Upgrade script from ART 2.3.1 to ART 2.4
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . support cc and bcc fields for email jobs
--          . remove object_group_id field from art_all_sources
--          . rename text_info field in art_all_sources
--          . rename field_class field in art_query_fields
--          . improve handling or numeric rule values
--          . support dynamic recipients for jobs
--
-- ------------------------------------------------


-- update database version setting
UPDATE ART_SETTINGS SET SETTING_VALUE='2.4' WHERE SETTING_NAME='database version';

-- add cc and bcc fields for email jobs
ALTER TABLE ART_JOBS ADD MAIL_CC VARCHAR(254);
ALTER TABLE ART_JOBS ADD MAIL_BCC VARCHAR(254);

-- remove object_group_id field from art_all_sources
ALTER TABLE ART_ALL_SOURCES DROP COLUMN OBJECT_GROUP_ID;

-- rename text_info field in art_all_sources
ALTER TABLE ART_ALL_SOURCES ADD SOURCE_INFO VARCHAR(4000);
UPDATE ART_ALL_SOURCES SET SOURCE_INFO=TEXT_INFO;
ALTER TABLE ART_ALL_SOURCES DROP COLUMN TEXT_INFO;

-- rename field_class field in art_query_fields
ALTER TABLE ART_QUERY_FIELDS ADD PARAM_DATA_TYPE VARCHAR(15);
UPDATE ART_QUERY_FIELDS SET PARAM_DATA_TYPE=FIELD_CLASS;
ALTER TABLE ART_QUERY_FIELDS DROP COLUMN FIELD_CLASS;

-- improve handling or numeric rule values
ALTER TABLE ART_QUERY_RULES ADD FIELD_DATA_TYPE VARCHAR(15);

-- support dynamic recipients for jobs
ALTER TABLE ART_JOBS ADD RECIPIENTS_QUERY_ID INTEGER;



