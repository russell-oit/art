-- Migration script from Art 1.11 to Art 1.11rev1
--
-- Purpose: create/update the tables needed to 
--          . allow drill down queries to be configured to open in a new window or the same window as the main query
--          . better shared access manangement for split jobs

-- ------------------------------------------------

-- change to ART_DRILLDOWN_QUERIES to support configuration of drill down target
ALTER TABLE ART_DRILLDOWN_QUERIES ADD OPEN_IN_NEW_WINDOW VARCHAR(1);

-- change to ART_SHARED_JOBS to better manage shared access for split jobs
ALTER TABLE ART_SHARED_JOBS ADD USER_GROUP_ID INTEGER;



