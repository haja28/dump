-- Fix typo in column name: 'tumbnail_image_path' -> 'thumbnail_image_path'
-- The database column was created with a typo (missing 'h')
-- Run this SQL to fix the issue:

ALTER TABLE menu_item_images
CHANGE COLUMN tumbnail_image_path thumbnail_image_path VARCHAR(500) NULL;

