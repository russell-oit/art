-- Migration script from Art 1.8/1.9 to Art 1.10
-- Description: rename column named rule to rule_name, rule is a keyword
--              in some databases (i.e. sqlserver)
--
-- For Oracle you can use the following three statements:
--    alter table ART_QUERY_RULES rename column RULE to RULE_NAME;
--    alter table ART_USER_RULES rename column RULE to RULE_NAME;
--    alter table ART_RULES rename column RULE to RULE_NAME;
--
-- The below is a set of generic SQL-92 statements that can be used
-- to update the Art Repository 
 
--create new RULE_NAME column
ALTER TABLE ART_QUERY_RULES ADD COLUMN RULE_NAME VARCHAR(15) NOT NULL;
ALTER TABLE ART_USER_RULES ADD COLUMN RULE_NAME VARCHAR(15) NOT NULL;
ALTER TABLE ART_RULES ADD COLUMN RULE_NAME VARCHAR(15) NOT NULL;
--update the new column with data from the rule column
UPDATE ART_QUERY_RULES SET RULE_NAME=RULE;
UPDATE ART_USER_RULES SET RULE_NAME=RULE;
UPDATE ART_RULES SET RULE_NAME=RULE;
--create indexes to use the new rule_name column
CREATE INDEX user_rules_indx2 ON ART_USER_RULES ( USERNAME, RULE_NAME);
--drop primary key constraints on the rule column
ALTER TABLE ART_QUERY_RULES DROP PRIMARY KEY;
ALTER TABLE ART_RULES DROP PRIMARY KEY;
--recreate primary keys on the rule_name column
ALTER TABLE ART_QUERY_RULES ADD CONSTRAINT pk_art_query_rules PRIMARY KEY ( QUERY_ID, RULE_NAME);
ALTER TABLE ART_RULES ADD CONSTRAINT pk_art_rules PRIMARY KEY ( RULE_NAME);

-- 20120504. delete old columns. not used anymore and they have not null constraint
ALTER TABLE ART_QUERY_RULES DROP COLUMN RULE;
ALTER TABLE ART_USER_RULES DROP COLUMN RULE;
ALTER TABLE ART_RULES DROP COLUMN RULE;

