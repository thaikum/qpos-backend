-- Fix: "Data truncated for column 'stock_take_type'"
--
-- The table was created with ENUM('CATEGORY','FULL','ITEMS','RANDOM','SUB_CATEGORY').
-- The app now also uses ABC_CLASS_A / ABC_CLASS_B / ABC_CLASS_C. MySQL rejects any
-- value not listed on the ENUM, which causes the insert to fail.
--
-- Run ONE of the options below against your database (same schema the app uses).

-- ---------------------------------------------------------------------------
-- Option A (recommended): store types as VARCHAR — no ENUM maintenance when enums grow
-- ---------------------------------------------------------------------------
ALTER TABLE stock_take
  MODIFY COLUMN stock_take_type VARCHAR(32) NULL;

-- ---------------------------------------------------------------------------
-- Option B: keep ENUM — extend the list (must be updated again if you add more Java enum values)
-- ---------------------------------------------------------------------------
-- ALTER TABLE stock_take
--   MODIFY COLUMN stock_take_type ENUM(
--     'CATEGORY',
--     'FULL',
--     'ITEMS',
--     'RANDOM',
--     'SUB_CATEGORY',
--     'ABC_CLASS_A',
--     'ABC_CLASS_B',
--     'ABC_CLASS_C'
--   ) DEFAULT NULL;
