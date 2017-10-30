-- Upgrade script from ART 3.0.1 to ART 3.1

-- CHANGES:
-- update database version
-- add lov use dynamic datasource column
-- increase size of output format columns
-- increase size of job param value column
-- increase size of query rules field name column
-- increase size of parameter default value column
-- increase size of job subject and fixed file name columns
-- add open and modify password fields for reports

-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
--
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>
-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.1-snapshot';

-- add lov use dynamic datasource column
ALTER TABLE ART_QUERIES ADD LOV_USE_DYNAMIC_DATASOURCE INTEGER;
UPDATE ART_QUERIES SET LOV_USE_DYNAMIC_DATASOURCE=0;

-- increase size of output format columns
ALTER TABLE ART_DRILLDOWN_QUERIES MODIFY OUTPUT_FORMAT VARCHAR(50);
ALTER TABLE ART_JOBS MODIFY OUTPUT_FORMAT VARCHAR(50);
ALTER TABLE ART_QUERIES MODIFY DEFAULT_REPORT_FORMAT VARCHAR(50);

-- increase size of job param value column
ALTER TABLE ART_JOBS_PARAMETERS MODIFY PARAM_VALUE VARCHAR(4000);

-- increase size of query rules field name column
ALTER TABLE ART_QUERY_RULES MODIFY FIELD_NAME VARCHAR(100);

-- increase size of parameter default value column
ALTER TABLE ART_PARAMETERS MODIFY DEFAULT_VALUE VARCHAR(4000);

-- increase size of job subject and fixed file name columns
ALTER TABLE ART_JOBS MODIFY SUBJECT VARCHAR(1000);
ALTER TABLE ART_JOBS MODIFY FIXED_FILE_NAME VARCHAR(1000);

-- add csv report type
INSERT INTO ART_REPORT_TYPES VALUES (152,'CSV');

-- add open and modify password fields for reports
ALTER TABLE ART_QUERIES ADD OPEN_PASSWORD VARCHAR(100);
ALTER TABLE ART_QUERIES ADD MODIFY_PASSWORD VARCHAR(100);
