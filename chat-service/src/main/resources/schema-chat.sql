-- =====================================================
-- Chat System Database Schema
-- =====================================================
-- Run this script to create the chat system tables
-- =====================================================

-- Chat Users Table (for tracking online status and preferences)
CREATE TABLE IF NOT EXISTS chat_users (
    chat_user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type ENUM('CUSTOMER', 'KITCHEN', 'SYSTEM') NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    is_online BOOLEAN DEFAULT FALSE,
    last_seen DATETIME,
    push_token VARCHAR(500),
    notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_type (user_id, user_type),
    INDEX idx_chat_user (user_id),
    INDEX idx_chat_user_type (user_type),
    INDEX idx_chat_user_online (is_online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Conversations Table
CREATE TABLE IF NOT EXISTS conversations (
    conversation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    customer_id BIGINT NOT NULL,
    kitchen_id BIGINT NOT NULL,
    kitchen_user_id BIGINT,
    title VARCHAR(255),
    status ENUM('ACTIVE', 'WAITING', 'RESOLVED', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    customer_unread_count INT DEFAULT 0,
    kitchen_unread_count INT DEFAULT 0,
    last_message_preview VARCHAR(255),
    last_message_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    archived_at DATETIME,
    INDEX idx_conversation_order (order_id),
    INDEX idx_conversation_customer (customer_id),
    INDEX idx_conversation_kitchen (kitchen_id),
    INDEX idx_conversation_status (status),
    INDEX idx_conversation_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages Table
CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_type ENUM('CUSTOMER', 'KITCHEN', 'SYSTEM') NOT NULL,
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'ORDER_UPDATE', 'SYSTEM_NOTIFICATION', 'QUICK_REPLY') NOT NULL DEFAULT 'TEXT',
    status ENUM('SENT', 'DELIVERED', 'READ') NOT NULL DEFAULT 'SENT',
    attachment_url VARCHAR(500),
    attachment_type VARCHAR(50),
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at DATETIME,
    read_at DATETIME,
    edited_at DATETIME,
    deleted_at DATETIME,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_for_everyone BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    INDEX idx_message_conversation (conversation_id),
    INDEX idx_message_sender (sender_id),
    INDEX idx_message_sent_at (sent_at),
    INDEX idx_message_type (message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat Notifications Table
CREATE TABLE IF NOT EXISTS chat_notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type ENUM('CUSTOMER', 'KITCHEN', 'SYSTEM') NOT NULL,
    conversation_id BIGINT,
    message_id BIGINT,
    type ENUM('NEW_CONVERSATION', 'NEW_MESSAGE', 'ORDER_UPDATE', 'MESSAGE_READ', 'TYPING_INDICATOR') NOT NULL,
    title VARCHAR(100) NOT NULL,
    body VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_conversation (conversation_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data (Optional - for testing)
-- =====================================================

-- Insert sample chat user
-- INSERT INTO chat_users (user_id, user_type, display_name, is_online)
-- VALUES (1, 'CUSTOMER', 'John Doe', false);

-- Insert sample conversation
-- INSERT INTO conversations (order_id, customer_id, kitchen_id, title, status)
-- VALUES (1, 1, 1, 'Order #1001 Chat', 'ACTIVE');

-- Insert sample message
-- INSERT INTO messages (conversation_id, sender_id, sender_type, content, message_type)
-- VALUES (1, 1, 'CUSTOMER', 'Hello, I have a question about my order.', 'TEXT');
