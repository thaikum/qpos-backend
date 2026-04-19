-- Stock take reconciliation: align with shop_account only + sensible defaults
-- Run against your DB after loading a dump (e.g. 2026-04-05-backup.sql).
--
-- Background: legacy schemas sometimes declared duplicate FOREIGN KEYs on the same
-- column pointing at both `account` and `shop_account`. Hibernate/JPA uses
-- shop_account; drop the constraints that reference `account` if they exist.
--
-- Replace constraint names if yours differ (SHOW CREATE TABLE stock_take_recon_type_config;).

SET FOREIGN_KEY_CHECKS = 0;

-- Drop FKs to global `account` table (keep shop_account FKs). If a line errors with
-- "Unknown foreign key", your dump already removed it — skip that line.
ALTER TABLE `stock_take_recon_type_config` DROP FOREIGN KEY `FK6ubso46x80qamfd5k2budryh`;
ALTER TABLE `stock_take_recon_type_config` DROP FOREIGN KEY `FK910k5xyus53aj41029vrl2ryn`;
ALTER TABLE `stock_take_recon_type_config` DROP FOREIGN KEY `FKrik0e46oml23ljc8e4tspj1kn`;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Default shop_account ids (shop_id = 1 in 2026-04-05-backup.sql)
--   3  = COST OF GOODS (system default; journal credits this for write-offs)
--   4  = CASH
--   12 = OTHER EXPENSES (typical shrinkage / damage / spoilage expense bucket)
--   13 = SHORTAGE (liability — e.g. staff shortage)
--   14 = Overage (asset — clearing for unexplained extra stock)
-- Adjust ids if your shop_account table differs.
-- ---------------------------------------------------------------------------

-- UNRECORDED_SALE: recognise revenue + debit cash (or similar); COGS handled in sale pattern
UPDATE `stock_take_recon_type_config`
SET
  `apply_penalty` = 0,
  `create_sale` = 1,
  `has_financial_impact` = 1,
  `stock_overage_cause` = NULL,
  `balancing_account_id` = 4,
  `expense_account_id` = NULL,
  `penalty_account_id` = NULL,
  `is_deleted` = 0
WHERE `stock_take_recon_type` = 'UNRECORDED_SALE';

-- Shrinkage / write-off types: debit expense (12), credit COGS via posting code (uses default COGS)
UPDATE `stock_take_recon_type_config`
SET
  `apply_penalty` = 0,
  `create_sale` = 0,
  `has_financial_impact` = 1,
  `stock_overage_cause` = NULL,
  `expense_account_id` = 12,
  `balancing_account_id` = 12,
  `penalty_account_id` = NULL,
  `is_deleted` = 0
WHERE `stock_take_recon_type` IN ('DAMAGED_ITEMS', 'MISSING_ITEMS', 'INTERNAL_USE', 'EXPIRED_ITEMS');

-- EXCESS: optional; link overage clearing account when you post financial impact
UPDATE `stock_take_recon_type_config`
SET
  `apply_penalty` = 0,
  `create_sale` = 0,
  `has_financial_impact` = 0,
  `stock_overage_cause` = 'COUNTING_ERROR',
  `balancing_account_id` = 14,
  `expense_account_id` = NULL,
  `penalty_account_id` = NULL,
  `is_deleted` = 0
WHERE `stock_take_recon_type` = 'EXCESS_ITEMS';

-- Ensure EXPIRED_ITEMS row exists (enum supports it; older DBs may omit the row)
INSERT INTO `stock_take_recon_type_config` (
  `apply_penalty`,
  `create_sale`,
  `has_financial_impact`,
  `stock_overage_cause`,
  `stock_take_recon_type`,
  `balancing_account_id`,
  `expense_account_id`,
  `penalty_account_id`,
  `is_deleted`
)
SELECT
  0,
  0,
  1,
  NULL,
  'EXPIRED_ITEMS',
  12,
  12,
  NULL,
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `stock_take_recon_type_config` WHERE `stock_take_recon_type` = 'EXPIRED_ITEMS'
);
