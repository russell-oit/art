-- Upgrade script from ART 3.0 to ART 3.0.1

-- CHANGES:
-- update database version
-- add ftp connection type column
-- add omit title row column

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.0.1';

-- add ftp connection type column
ALTER TABLE ART_FTP_SERVERS ADD CONNECTION_TYPE VARCHAR(20);
UPDATE ART_FTP_SERVERS SET CONNECTION_TYPE='FTP';

-- add omit title row column
ALTER TABLE ART_QUERIES ADD OMIT_TITLE_ROW INTEGER;
UPDATE ART_QUERIES SET OMIT_TITLE_ROW=0;