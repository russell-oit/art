-- Upgrade script from ART 2.5.2 to ART 3.0
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . reset x_axis_label for non-graph queries
--          . allow datasources to be disabled
--
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='3.0-alpha1' WHERE SETTING_NAME='database version';

-- reset x_axis_label for non-graph queries
UPDATE ART_QUERIES SET X_AXIS_LABEL='' WHERE QUERY_TYPE>=0;

-- allow datasources to be disabled
ALTER TABLE ART_DATABASES ADD ACTIVE_STATUS VARCHAR(1);
UPDATE ART_DATABASES SET ACTIVE_STATUS='A';





