-- Upgrade script from ART 3.1 to ART 3.2

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.2-snapshot';

-- add reference report types
INSERT INTO ART_REPORT_TYPES VALUES (153,'Velocity');
INSERT INTO ART_REPORT_TYPES VALUES (154,'OrgChart: Database');
INSERT INTO ART_REPORT_TYPES VALUES (155,'OrgChart: JSON');
INSERT INTO ART_REPORT_TYPES VALUES (156,'OrgChart: List');
INSERT INTO ART_REPORT_TYPES VALUES (157,'OrgChart: Ajax');