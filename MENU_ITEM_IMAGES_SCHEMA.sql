-- =====================================================
-- MENU ITEM IMAGES TABLE - Multiple Images per Menu Item
-- =====================================================
-- This table stores menu item images with thumbnail support
-- Each image has a UUID for unique identification across kitchens

CREATE TABLE IF NOT EXISTS menu_item_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id INT NOT NULL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    original_image_path VARCHAR(500) NOT NULL,
    thumbnail_image_path VARCHAR(500) NULL,  -- Nullable: thumbnails are generated on demand
    content_type VARCHAR(50),
    file_size BIGINT,
    image_width INT,
    image_height INT,
    thumbnail_width INT,
    thumbnail_height INT,
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Key
    FOREIGN KEY (menu_item_id) REFERENCES kitchen_menu(item_id) ON DELETE CASCADE,

    -- Indexes for performance
    INDEX idx_menu_item_id (menu_item_id),
    INDEX idx_uuid (uuid),
    INDEX idx_is_primary (is_primary),
    INDEX idx_is_active (is_active),
    INDEX idx_display_order (display_order)
);

-- Add comment to describe the table
ALTER TABLE menu_item_images COMMENT = 'Stores multiple images per menu item with thumbnail support';

