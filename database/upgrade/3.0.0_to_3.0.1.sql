-- Upgrade script from ART 3.0 to ART 3.1

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.1-snapshot');
