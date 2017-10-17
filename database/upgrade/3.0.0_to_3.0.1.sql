-- Upgrade script from ART 3.0 to ART 3.0.1

-- CHANGES:
-- update database version
-- add ftp connection type column

-- ------------------------------------------------


-- update database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.1-snapshot');

-- add ftp connection type column
ALTER TABLE ART_FTP_SERVERS ADD CONNECTION_TYPE VARCHAR(20);
UPDATE ART_FTP_SERVERS SET CONNECTION_TYPE='FTP';
