Chat System Development Prompt: Customer-Kitchen Communication
Project Overview
Develop a real-time chat messaging system in Java that enables communication between Customers and Kitchen Users with conversation management, notifications, and message tracking.

Core Requirements
1. User Types
   Customer: Orders food and communicates with kitchen staff
   Kitchen User: Receives orders and responds to customer queries
2. Key Functionality
   A. Chat Initiation
   Customers can initiate a new chat conversation when placing an order
   System automatically creates a conversation thread linked to order ID
   Kitchen users receive notification of new chat requests
   Support for 1-to-1 chat (Customer ↔ Kitchen User)
   Optional: Multiple kitchen users can join the same conversation
   B. Message Features
   Send text messages in real-time
   Display message timestamp with date and time
   Message status indicators:
   Sent ✓
   Delivered ✓✓
   Read ✓✓ (blue ticks)
   Support for message types:
   Text messages
   Order updates
   System notifications (e.g., "Order accepted", "Order ready")
   Optional: Image attachments for order clarification
   C. Notification System
   Push notifications for new messages
   Badge counter showing unread message count
   In-app notifications with sound/vibration
   Notification types:
   New conversation initiated
   New message received
   Order status updates
   Mark notifications as read
   Clear all notifications option
   D. Read Status Tracking
   Track when messages are delivered to recipient
   Track when messages are read/viewed
   Display "typing..." indicator when user is composing
   Last seen timestamp for each user
   Update read receipts in real-time
   E. Conversation Management
   Conversation List View:

Show all active conversations
Display last message preview
Show unread count per conversation
Sort by most recent activity
Filter by: Active, Archived, Unread
Conversation Thread View:

Display messages in chronological order
Group messages by date (e.g., "Today", "Yesterday", "Jan 15")
Show sender avatar and name
Distinguish between sent/received messages (different alignment/colors)
Auto-scroll to latest message
Message Persistence:

Store all messages in database
Retrieve conversation history
Pagination for loading older messages
Search within conversation
F. Date and Timestamp Management
Display relative time for recent messages:
"Just now" (< 1 minute)
"5 minutes ago"
"Today at 2:30 PM"
"Yesterday at 10:15 AM"
"Jan 15 at 3:45 PM" (older messages)
Group messages by date separators
Show full timestamp on message long-press/hover
3. Technical Architecture
   Backend (Java)
   Framework: Spring Boot
   Real-time Communication:
   WebSocket (using Spring WebSocket + STOMP)
   OR Server-Sent Events (SSE)
   Database:
   Conversations Table: conversation_id, order_id, customer_id, kitchen_user_id, created_at, updated_at, status
   Messages Table: message_id, conversation_id, sender_id, sender_type, content, message_type, sent_at, delivered_at, read_at
   Notifications Table: notification_id, user_id, conversation_id, message_id, type, is_read, created_at
   Users Table: user_id, name, user_type (CUSTOMER/KITCHEN), last_seen, online_status
   REST API Endpoints
   Code
   POST   /api/conversations                 # Initiate new chat
   GET    /api/conversations                 # Get all conversations for user
   GET    /api/conversations/{id}/messages   # Get messages in conversation
   POST   /api/conversations/{id}/messages   # Send new message
   PUT    /api/messages/{id}/read            # Mark message as read
   GET    /api/notifications                 # Get user notifications
   PUT    /api/notifications/{id}/read       # Mark notification as read
   DELETE /api/notifications                 # Clear all notifications
   WebSocket Events
   Code
   /topic/conversations/{id}  # Subscribe to conversation updates
   /app/message.send          # Send message
   /app/message.read          # Update read status
   /app/typing                # Typing indicator
   /user/queue/notifications  # Private notification channel
4. Key Classes & Components
   Java
   // Domain Models
- Conversation
- Message
- Notification
- User (Customer, KitchenUser)

// Services
- ConversationService (initiate, list, archive)
- MessageService (send, retrieve, updateReadStatus)
- NotificationService (create, send, markRead)
- WebSocketMessageHandler

// Controllers
- ConversationController
- MessageController
- NotificationController
- WebSocketController

// Repositories
- ConversationRepository
- MessageRepository
- NotificationRepository
- UserRepository
5. Frontend Requirements optimize for flutter based frontend framework
   Chat List Screen: Display all conversations
   Chat Thread Screen: Message exchange interface
   Notification Panel: Show unread notifications
   Input Area: Text input with send button
   Real-time Updates: Auto-refresh on new messages
6. Additional Features
   Search: Search messages within conversations
   Archive: Archive completed order conversations
   Block/Report: Safety features
   Auto-responses: Pre-defined quick replies for kitchen users
   Conversation Status: Active, Waiting, Resolved, Archived
   Message Editing: Edit sent messages (within time limit)
   Message Deletion: Delete messages (for self or everyone)
7. Non-Functional Requirements
   Performance: Messages delivered in < 1 second
   Scalability: Support 1000+ concurrent conversations
   Security:
   Authenticate all WebSocket connections
   Encrypt messages in transit (TLS/SSL)
   Validate user permissions (customers only see their chats)
   Reliability: Message delivery guarantee with retry mechanism
   Offline Support: Queue messages when offline, send when reconnected