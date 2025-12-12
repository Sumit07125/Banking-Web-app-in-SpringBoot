-- Migration: add profile columns to Account and create login_history & cheque_request (if needed)
-- Run this against your MySQL database used by the application.

-- Add profile columns to account table (mobile, mailing address, nominee, frozen, active)
ALTER TABLE `account`
  ADD COLUMN IF NOT EXISTS `mobile_number` VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `mailing_address` VARCHAR(255) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `nominee_name` VARCHAR(255) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `frozen` TINYINT(1) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `active` TINYINT(1) DEFAULT 1;

-- Create login_history table to record login events
CREATE TABLE IF NOT EXISTS `login_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `account_number` VARCHAR(64) NOT NULL,
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_address` VARCHAR(64),
  `user_agent` VARCHAR(1024),
  `success` TINYINT(1) DEFAULT 1,
  INDEX (`account_number`)
);

-- Ensure cheque_request table exists (same structure as ChequeRequest entity)
CREATE TABLE IF NOT EXISTS `cheque_request` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `account_number` VARCHAR(64),
  `type` VARCHAR(50),
  `cheque_number` VARCHAR(64),
  `status` VARCHAR(50),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (`account_number`)
);

-- Note: If your physical naming strategy maps camelCase -> snake_case,
-- these snake_case column names will match the JPA mappings. If your
-- database uses different naming (e.g. mobileNumber), adjust the column
-- names accordingly.
