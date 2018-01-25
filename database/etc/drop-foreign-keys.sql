-- Drop foreign keys from ART database tables

-- NOTES:
-- for mysql, replace DROP CONSTRAINT with DROP FOREIGN KEY

-- ------------------------

ALTER TABLE ART_QUERIES DROP CONSTRAINT aq_fk_ds_id;
ALTER TABLE ART_USERS DROP CONSTRAINT au_fk_acc_lvl;
