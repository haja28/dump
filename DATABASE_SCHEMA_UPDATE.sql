-- =====================================================
-- DATABASE SCHEMA UPDATE - ADD MISSING COLUMNS
-- =====================================================
-- This script updates the existing kitchens table to add
-- missing columns that are required by the Kitchen entity

USE makanforyou;

-- =====================================================
-- Step 1: Add owner_user_id column if it doesn't exist
-- =====================================================
ALTER TABLE kitchens
ADD COLUMN owner_user_id INT NOT NULL
AFTER kitchen_owner_name;

-- =====================================================
-- Step 2: Add approval_status column if it doesn't exist
-- =====================================================
ALTER TABLE kitchens
ADD COLUMN approval_status ENUM('PENDING', 'APPROVED', 'REJECTED')
DEFAULT 'PENDING' NOT NULL
AFTER verified;

-- =====================================================
-- Step 3: Add Foreign Key constraint for owner_user_id
-- =====================================================
ALTER TABLE kitchens
ADD CONSTRAINT fk_owner_user_id
FOREIGN KEY (owner_user_id)
REFERENCES users(user_id) ON DELETE CASCADE;

-- =====================================================
-- Step 4: Add index for owner_user_id
-- =====================================================
ALTER TABLE kitchens
ADD INDEX idx_owner_user_id (owner_user_id);

-- =====================================================
-- Verification Query - Check kitchens table structure
-- =====================================================
DESCRIBE kitchens;

-- =====================================================
-- Show column details
-- =====================================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'kitchens'
  AND TABLE_SCHEMA = 'makanforyou'
ORDER BY ORDINAL_POSITION;
