# Messaging Service

Spring Boot microservice for peer-to-peer messaging between users. Provides a REST API for sending messages, retrieving conversation history, and marking messages as read. Real-time delivery on the client side is handled via Supabase Realtime.

## Tech Stack

- **Java 21** / Spring Boot 4.0.6
- **Spring Security** with stateless JWT (JJWT 0.12.6)
- **PostgreSQL** (Supabase) via Spring Data JPA
- **Gradle** build tool

## API Endpoints

Base path: `/messages`

All endpoints require `Authorization: Bearer <token>`.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/messages` | Send a message to another user |
| GET | `/messages/conversations` | List all conversation partners with last message and unread count |
| GET | `/messages/{otherUserId}` | Fetch paginated message history with a specific user |
| PATCH | `/messages/{messageId}/read` | Mark a received message as read |

### Request / Response Examples

**POST /messages**
```json
// Request
{
  "receiverId": 42,
  "content": "Hey, is this still available?"
}

// Response 201
{
  "id": 101,
  "senderId": 1,
  "receiverId": 42,
  "content": "Hey, is this still available?",
  "sentAt": "2026-06-10T14:00:00Z",
  "readAt": null
}
```

**GET /messages/conversations**
```json
// Response 200
[
  {
    "otherUserId": 42,
    "lastMessage": "Hey, is this still available?",
    "lastMessageAt": "2026-06-10T14:00:00Z",
    "unreadCount": 2
  }
]
```

**GET /messages/{otherUserId}?page=0&size=20**
```json
// Response 200
{
  "otherUserId": 42,
  "messages": [ /* MessageResponse array, newest first */ ],
  "currentPage": 0,
  "totalPages": 3,
  "totalElements": 54
}
```

**PATCH /messages/{messageId}/read**
```json
// Response 200 — updated MessageResponse with readAt set
{
  "id": 101,
  "senderId": 42,
  "receiverId": 1,
  "content": "Yes, still available!",
  "sentAt": "2026-06-10T14:05:00Z",
  "readAt": "2026-06-10T14:10:00Z"
}
```

Only the receiver of a message can mark it as read; any other attempt returns **403**.

## Query Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page index for conversation history |
| `size` | `20` | Number of messages per page |

## Error Responses

| Status | Cause |
|--------|-------|
| 400 | Validation failure (missing or malformed fields) |
| 401 | Missing or invalid JWT |
| 403 | Attempting to mark someone else's message as read |
| 404 | Message not found |

## Database Schema

```sql
CREATE TABLE message (
  id          SERIAL PRIMARY KEY,
  sender_id   INT         NOT NULL REFERENCES "user"(id),
  receiver_id INT         NOT NULL REFERENCES "user"(id),
  content     TEXT        NOT NULL,
  sent_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  read_at     TIMESTAMPTZ          -- NULL means unread
);

-- Index for efficient bidirectional conversation lookups
CREATE INDEX idx_message_conversation
  ON message (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), sent_at);
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `MESSAGING_PORT` | `8082` | Server port |
| `DB_URL` | Supabase pooler URL | JDBC connection string |
| `DB_USER` | `postgres.*` | Database username |
| `DB_PASSWORD` | — | Database password |
| `DDL_AUTO` | `update` | Hibernate DDL strategy |
| `JWT_SECRET` | — | HMAC-SHA signing key — must match the auth service **(set in production)** |
| `JWT_EXPIRY_DAYS` | `30` | Token lifetime in days |

## Running Locally

```bash
# Build
./gradlew bootJar

# Run (with env vars set)
java -jar build/libs/messaging-service-*.jar
```

Service starts on port **8082** by default.

## Docker

```bash
docker build -t messaging-service .
docker run -p 8082:8082 \
  -e JWT_SECRET=your-secret \
  -e DB_PASSWORD=your-password \
  messaging-service
```

## Notes

- Messages are **immutable** once sent; there is no delete or edit endpoint.
- Real-time push to clients uses **Supabase Realtime** subscriptions — the backend does not manage WebSocket connections.
- The `JWT_SECRET` must be identical to the one used by the authentication service so that tokens issued there are accepted here.