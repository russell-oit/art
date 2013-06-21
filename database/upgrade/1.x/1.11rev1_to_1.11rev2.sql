-- Migration script from Art 1.11rev1 to Art 1.11rev2
--
-- Purpose: create/update the tables needed to 
--          . allow chained parameter value to come from a different parameter from the previous one in the chained parameter sequence

-- ------------------------------------------------

-- change to ART_QUERY_FIELDS to support chained value position
ALTER TABLE ART_QUERY_FIELDS ADD CHAINED_VALUE_POSITION INTEGER;




