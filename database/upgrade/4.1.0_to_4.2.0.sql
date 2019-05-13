-- Upgrade script from ART 4.1 to ART 4.2

-- CHANGES:
-- update database version
-- add use_api permission
-- add allow select parameters column
-- update drilldown output format column
-- insert new report type

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.2';

-- add use_api permission
INSERT INTO ART_PERMISSIONS VALUES(31, 'use_api');

-- add allow select parameters column
ALTER TABLE ART_DRILLDOWN_QUERIES ADD ALLOW_SELECT_PARAMETERS INTEGER;
UPDATE ART_DRILLDOWN_QUERIES SET ALLOW_SELECT_PARAMETERS=1;

-- update drilldown output format column
UPDATE ART_DRILLDOWN_QUERIES SET OUTPUT_FORMAT='default' WHERE OUTPUT_FORMAT='ALL' or OUTPUT_FORMAT IS NULL;

-- insert new report type
INSERT INTO ART_REPORT_TYPES VALUES (162,'File');
