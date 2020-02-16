-- Upgrade script from ART 4.10 to ART 4.11

-- CHANGES:
-- update database version
-- add allow_null column
-- add run_immediately column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.11';

-- add allow_null column
ALTER TABLE ART_PARAMETERS ADD ALLOW_NULL INTEGER;
UPDATE ART_PARAMETERS SET ALLOW_NULL=0;

-- add run_immediately column
ALTER TABLE ART_DRILLDOWN_QUERIES ADD RUN_IMMEDIATELY INTEGER;
UPDATE ART_DRILLDOWN_QUERIES SET RUN_IMMEDIATELY=1;
