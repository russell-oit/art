-- Upgrade script from ART 3.6 to ART 3.7

-- CHANGES:
-- update database version
-- add json key file field

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.7';

-- add json key file field
ALTER TABLE ART_DESTINATIONS ADD GOOGLE_JSON_KEY_FILE VARCHAR(100);
