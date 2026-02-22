-- =====================================================
-- FIX: Make thumbnail_image_path nullable
-- =====================================================
-- The thumbnail is generated on demand, so it should be nullable
-- This fixes: Column 'thumbnail_image_path' cannot be null error

ALTER TABLE menu_item_images
MODIFY COLUMN thumbnail_image_path VARCHAR(500) NULL;

-- Verify the change
DESCRIBE menu_item_images;

