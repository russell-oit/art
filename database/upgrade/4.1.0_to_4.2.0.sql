-- Upgrade script from ART 4.1 to ART 4.2

-- CHANGES:
-- update database version
-- add use_api permission

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.2-snapshot';

-- add use_api permission
INSERT INTO ART_PERMISSIONS VALUES(31, 'use_api');
