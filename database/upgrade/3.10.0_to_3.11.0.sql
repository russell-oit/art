-- Upgrade script from ART 3.10 to ART 3.11

-- CHANGES:
-- update database version
-- add public user column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.11-snapshot';

-- add public user column
ALTER TABLE ART_USERS ADD PUBLIC_USER INTEGER;
UPDATE ART_USERS SET PUBLIC_USER=0;
UPDATE ART_USERS SET PUBLIC_USER=1 WHERE USERNAME='public_user';
