# Chat Service

Real-time chat messaging system for Customer-Kitchen communication in the Makan For You platform.

## Features

- **Real-time Messaging**: WebSocket-based real-time chat using STOMP protocol
- **Conversation Management**: Create, list, search, and archive conversations
- **Message Features**: Send, edit, delete messages with status tracking
- **Notifications**: In-app notifications for new messages
- **Read Receipts**: Track message delivery and read status
- **Typing Indicators**: Real-time typing status updates

## Port

- **Local**: `8086`
- **Via API Gateway**: `8080`

## API Endpoints

### Conversations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/conversations` | Create new conversation |
| GET | `/api/v1/conversations/customer` | Get customer's conversations |
| GET | `/api/v1/conversations/kitchen` | Get kitchen's conversations |
| GET | `/api/v1/conversations/{id}` | Get conversation by ID |
| GET | `/api/v1/conversations/unread` | Get unread conversations |
| GET | `/api/v1/conversations/search` | Search conversations |
| POST | `/api/v1/conversations/{id}/archive` | Archive conversation |

### Messages
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/conversations/{id}/messages` | Send message |
| GET | `/api/v1/conversations/{id}/messages` | Get messages (paginated) |
| GET | `/api/v1/conversations/{id}/messages/all` | Get all messages |
| PUT | `/api/v1/conversations/{id}/messages/read` | Mark as read |
| PATCH | `/api/v1/conversations/{id}/messages/{msgId}` | Edit message |
| DELETE | `/api/v1/conversations/{id}/messages/{msgId}` | Delete message |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/notifications` | Get all notifications |
| GET | `/api/v1/notifications/unread` | Get unread notifications |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read |
| PUT | `/api/v1/notifications/read-all` | Mark all as read |
| DELETE | `/api/v1/notifications` | Clear all notifications |

## WebSocket

### Connection
```
ws://localhost:8086/ws/chat
```

### Subscribe Topics
- `/topic/conversations/{conversationId}` - Conversation updates
- `/user/{userType}/{userId}/queue/notifications` - User notifications

### Send Destinations
- `/app/chat/{conversationId}/send` - Send message
- `/app/chat/{conversationId}/typing` - Typing indicator
- `/app/chat/{conversationId}/read` - Mark as read

## Required Headers

| Header | Description |
|--------|-------------|
| `X-User-Id` | Authenticated user's ID |
| `X-User-Type` | `CUSTOMER` or `KITCHEN` |
| `X-Kitchen-Id` | Kitchen ID (for kitchen endpoints) |

## Database Tables

- `conversations` - Chat conversations
- `messages` - Chat messages
- `chat_notifications` - User notifications
- `chat_users` - User presence/status

## Running the Service

```bash
cd chat-service
mvn spring-boot:run
```

## Swagger UI

Access API documentation at:
```
http://localhost:8086/swagger-ui.html
```

## Dependencies

- Spring Boot 3.2.0
- Spring WebSocket
- Spring Data JPA
- MySQL
- Lombok
- MapStruct
- SpringDoc OpenAPI
