# Chat Service API Documentation

## Overview

The Chat Service provides real-time messaging capabilities between Customers and Kitchen Users. It supports:
- Real-time messaging via WebSocket (STOMP)
- REST API for conversation and message management
- Push notifications
- Read receipts and typing indicators

## Base URL

- **Local Development**: `http://localhost:8086`
- **Via API Gateway**: `http://localhost:8080/chat`

## Authentication Headers

All endpoints require the following headers:
- `X-User-Id`: The authenticated user's ID
- `X-User-Type`: Either `CUSTOMER` or `KITCHEN`
- `X-Kitchen-Id`: Required for kitchen-specific endpoints

---

## REST API Endpoints

### Conversations

#### Create Conversation
```http
POST /api/v1/conversations
```

**Headers:**
- `X-User-Id`: Customer ID

**Request Body:**
```json
{
  "order_id": 123,
  "kitchen_id": 1,
  "title": "Order #1001 Chat",
  "initial_message": "Hi, I have a question about my order"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "conversation_id": 1,
    "order_id": 123,
    "customer_id": 1,
    "kitchen_id": 1,
    "title": "Order #1001 Chat",
    "status": "ACTIVE",
    "unread_count": 0,
    "created_at": "2024-01-15T10:30:00"
  },
  "message": "Conversation created successfully"
}
```

#### Get Customer Conversations
```http
GET /api/v1/conversations/customer?page=0&size=20
```

#### Get Kitchen Conversations
```http
GET /api/v1/conversations/kitchen?page=0&size=20
```

#### Get Active Conversations
```http
GET /api/v1/conversations/customer/active?page=0&size=20
GET /api/v1/conversations/kitchen/active?page=0&size=20
```

#### Get Unread Conversations
```http
GET /api/v1/conversations/unread?page=0&size=20
```

#### Search Conversations
```http
GET /api/v1/conversations/search?query=order&page=0&size=20
```

#### Archive Conversation
```http
POST /api/v1/conversations/{conversationId}/archive
```

#### Get Unread Count
```http
GET /api/v1/conversations/unread/count
```

---

### Messages

#### Send Message
```http
POST /api/v1/conversations/{conversationId}/messages
```

**Request Body:**
```json
{
  "content": "Hello, when will my order be ready?",
  "message_type": "TEXT",
  "attachment_url": null,
  "attachment_type": null
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "message_id": 1,
    "conversation_id": 1,
    "sender_id": 1,
    "sender_type": "CUSTOMER",
    "content": "Hello, when will my order be ready?",
    "message_type": "TEXT",
    "status": "SENT",
    "sent_at": "2024-01-15T10:30:00",
    "formatted_time": "Just now",
    "is_own_message": true
  }
}
```

#### Get Messages (Paginated)
```http
GET /api/v1/conversations/{conversationId}/messages?page=0&size=50
```

#### Get All Messages
```http
GET /api/v1/conversations/{conversationId}/messages/all
```

#### Get New Messages (After Timestamp)
```http
GET /api/v1/conversations/{conversationId}/messages/after?after=2024-01-15T10:30:00
```

#### Mark Messages as Read
```http
PUT /api/v1/conversations/{conversationId}/messages/read
```

#### Mark Messages as Delivered
```http
PUT /api/v1/conversations/{conversationId}/messages/delivered
```

#### Search Messages
```http
GET /api/v1/conversations/{conversationId}/messages/search?query=order&page=0&size=20
```

#### Edit Message
```http
PATCH /api/v1/conversations/{conversationId}/messages/{messageId}?content=Updated message
```

#### Delete Message
```http
DELETE /api/v1/conversations/{conversationId}/messages/{messageId}?forEveryone=false
```

---

### Notifications

#### Get All Notifications
```http
GET /api/v1/notifications?page=0&size=20
```

#### Get Unread Notifications
```http
GET /api/v1/notifications/unread?page=0&size=20
```

#### Get Unread Count
```http
GET /api/v1/notifications/unread/count
```

#### Mark Notification as Read
```http
PUT /api/v1/notifications/{notificationId}/read
```

#### Mark All as Read
```http
PUT /api/v1/notifications/read-all
```

#### Clear All Notifications
```http
DELETE /api/v1/notifications
```

---

## WebSocket API

### Connection

Connect to the WebSocket endpoint:
```
ws://localhost:8086/ws/chat
```

With SockJS fallback:
```
http://localhost:8086/ws/chat
```

### Subscription Topics

#### Subscribe to Conversation
```
/topic/conversations/{conversationId}
```

Receives events:
- `NEW_MESSAGE`
- `MESSAGE_DELIVERED`
- `MESSAGE_READ`
- `TYPING_START`
- `TYPING_STOP`
- `USER_ONLINE`
- `USER_OFFLINE`
- `CONVERSATION_UPDATED`

#### Subscribe to User Notifications
```
/user/{userType}/{userId}/queue/notifications
```

### Send Messages

#### Send Chat Message
```
/app/chat/{conversationId}/send
```

**Headers:**
- `userId`: Sender's user ID
- `userType`: `CUSTOMER` or `KITCHEN`

**Payload:**
```json
{
  "content": "Hello!",
  "message_type": "TEXT"
}
```

#### Send Typing Indicator
```
/app/chat/{conversationId}/typing
```

**Payload:**
```json
{
  "conversation_id": 1,
  "user_id": 1,
  "user_name": "John",
  "is_typing": true
}
```

#### Mark Messages as Read
```
/app/chat/{conversationId}/read
```

#### Mark Messages as Delivered
```
/app/chat/{conversationId}/delivered
```

---

## WebSocket Message Format

All WebSocket messages follow this format:

```json
{
  "event_type": "NEW_MESSAGE",
  "conversation_id": 1,
  "message": {
    "message_id": 1,
    "sender_id": 1,
    "sender_type": "CUSTOMER",
    "content": "Hello!",
    "message_type": "TEXT",
    "status": "SENT",
    "sent_at": "2024-01-15T10:30:00",
    "formatted_time": "Just now"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Event Types

| Event Type | Description |
|------------|-------------|
| `NEW_MESSAGE` | New message received |
| `MESSAGE_DELIVERED` | Message was delivered to recipient |
| `MESSAGE_READ` | Message was read by recipient |
| `TYPING_START` | User started typing |
| `TYPING_STOP` | User stopped typing |
| `USER_ONLINE` | User came online |
| `USER_OFFLINE` | User went offline |
| `CONVERSATION_UPDATED` | Conversation details updated |

---

## Message Types

| Type | Description |
|------|-------------|
| `TEXT` | Regular text message |
| `IMAGE` | Image attachment |
| `ORDER_UPDATE` | Order status update |
| `SYSTEM_NOTIFICATION` | System notification |
| `QUICK_REPLY` | Pre-defined quick reply |

---

## Message Status

| Status | Description | Indicator |
|--------|-------------|-----------|
| `SENT` | Message sent to server | ✓ |
| `DELIVERED` | Message delivered to recipient | ✓✓ |
| `READ` | Message read by recipient | ✓✓ (blue) |

---

## Conversation Status

| Status | Description |
|--------|-------------|
| `ACTIVE` | Active conversation |
| `WAITING` | Waiting for response |
| `RESOLVED` | Issue resolved |
| `ARCHIVED` | Archived/closed |

---

## Flutter Integration Example

### Connect to WebSocket

```dart
import 'package:stomp_dart_client/stomp.dart';

final stompClient = StompClient(
  config: StompConfig(
    url: 'ws://localhost:8086/ws/chat',
    onConnect: onConnect,
    onWebSocketError: (error) => print('WebSocket error: $error'),
  ),
);

void onConnect(StompFrame frame) {
  // Subscribe to conversation
  stompClient.subscribe(
    destination: '/topic/conversations/1',
    callback: (StompFrame frame) {
      final message = jsonDecode(frame.body!);
      handleWebSocketMessage(message);
    },
  );
}

void sendMessage(int conversationId, String content) {
  stompClient.send(
    destination: '/app/chat/$conversationId/send',
    body: jsonEncode({
      'content': content,
      'message_type': 'TEXT',
    }),
    headers: {
      'userId': '1',
      'userType': 'CUSTOMER',
    },
  );
}
```

### REST API Call

```dart
Future<void> createConversation(int kitchenId, int orderId) async {
  final response = await http.post(
    Uri.parse('http://localhost:8086/api/v1/conversations'),
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': '1',
      'X-User-Type': 'CUSTOMER',
    },
    body: jsonEncode({
      'kitchen_id': kitchenId,
      'order_id': orderId,
      'title': 'Order #$orderId Chat',
    }),
  );
  
  if (response.statusCode == 201) {
    final data = jsonDecode(response.body);
    print('Conversation created: ${data['data']['conversation_id']}');
  }
}
```

---

## Swagger UI

Access the interactive API documentation at:
- `http://localhost:8086/swagger-ui.html`
