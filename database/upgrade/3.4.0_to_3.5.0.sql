-- Upgrade script from ART 3.4 to ART 3.5

-- CHANGES:
-- update database version
-- add use groovy column
-- update "null" column strings with proper NULL
-- add template field to parameters table
-- add reference record for plotly.js report type

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.5-snapshot';

-- add use groovy column
ALTER TABLE ART_QUERIES ADD USE_GROOVY INTEGER;
UPDATE ART_QUERIES SET USE_GROOVY=0;

-- update "null" column strings with proper NULL
UPDATE ART_QUERIES SET CONTACT_PERSON = NULL WHERE CONTACT_PERSON='null';
UPDATE ART_QUERIES SET X_AXIS_LABEL = NULL WHERE X_AXIS_LABEL='null';
UPDATE ART_QUERIES SET Y_AXIS_LABEL = NULL WHERE Y_AXIS_LABEL='null';
UPDATE ART_QUERIES SET TEMPLATE = NULL WHERE TEMPLATE='null';
UPDATE ART_QUERIES SET XMLA_DATASOURCE = NULL WHERE XMLA_DATASOURCE='null';
UPDATE ART_QUERIES SET XMLA_CATALOG = NULL WHERE XMLA_CATALOG='null';

-- add template field to parameters table
ALTER TABLE ART_PARAMETERS ADD TEMPLATE VARCHAR(100);

-- add reference record for plotly.js report type
INSERT INTO ART_REPORT_TYPES VALUES (160,'Plotly.js');