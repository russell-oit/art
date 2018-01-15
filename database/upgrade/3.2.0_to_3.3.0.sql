-- Upgrade script from ART 3.2 to ART 3.3

-- CHANGES:
-- update database version
-- add job error notification to column
-- add reference records for new report types

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.3-snapshot';

-- add job error notification to column
ALTER TABLE ART_JOBS ADD ERROR_EMAIL_TO VARCHAR(500);

-- add reference records for new report types
INSERT INTO ART_REPORT_TYPES VALUES (158,'ReportEngine');
INSERT INTO ART_REPORT_TYPES VALUES (159,'ReportEngine: File');
