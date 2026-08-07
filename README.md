# Forum Backend API Service

A social network forum backend API service built on **Java Spring Boot**, featuring **PostgreSQL** database persistence, **Redis** distributed caching, and real-time messaging via **WebSocket STOMP**.

---

## 🛠️ Technology Stack
- **Language & Framework**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA.
- **Database**: PostgreSQL.
- **Cache & Sessions**: Redis (stores email verification OTPs, login brute-force locking status, and password reset tokens).
- **Real-Time Transmission**: WebSocket with STOMP protocol (SockJS support).
- **Cloud Storage**: Cloudinary SDK (for avatar uploads, post attachment uploads, and chat media).
- **Email Service**: JavaMailSender with Thymeleaf templates (for registration OTPs, reset password OTPs, and login alerts from unrecognized devices).

---

## 📁 Source Code Directory Structure
```
src/main/java/com/example/forum/
├── common/                  # Shared utilities and helpers
│   ├── dto/                 # Standard API response wrap (ApiResponse)
│   ├── exception/           # Global exception handling (GlobalExceptionHandler)
│   └── service/email/       # Email transmission & Thymeleaf template processing
├── controller/              # General routes and test controllers
├── domain/                  # JPA Database Entities & Enums
├── core/                    # Security configurations (JWT), Redis, and Websocket Config
└── feature/                 # Vertical-slice feature modules
    ├── auth/                # Register, login, Refresh Token, 2FA, OTP verification
    ├── user/                # Profile retrieval and updates
    ├── post/                # Post CRUD operations and media attachments
    ├── comment/             # Depth-capped comments and thread context retrieval
    ├── follow/              # User follow & follower listings
    ├── collection/          # Save posts to personal collections
    ├── notification/        # Activity notifications sent via WebSocket
    └── chat/                # Real-time chat messaging and member group management
```

---

## 🔌 API Endpoints Reference

### 🔑 Authentication (`/forum/auth`)
- `GET /me` - Get profile of currently authenticated user
- `POST /register` - Register a new user account
- `POST /verify-email` - Verify OTP to activate user account
- `PATCH /resend-verification-code` - Resend verification code via email
- `POST /login` - Login with credentials (checks brute-force locking limits)
- `POST /refresh-token` - Issue a new Access Token using a valid Refresh Token
- `POST /logout` - Log out and invalidate sessions
- `POST /forgot-password` - Request a password reset OTP
- `POST /reset-password` - Reset password using the OTP
- `POST /2fa-login` - Login using 2FA OTP code (Google Authenticator)

### 🔐 2FA Management (`/forum/user/2af`)
- `GET /setup` - Initialize Google Authenticator secret key and QR code image
- `POST /verify` - Verify OTP to enable 2FA protection
- `POST /disable-2fa` - Disable 2FA (requires password confirmation)
- `GET /isEnable` - Check user's 2FA status

### 📱 Devices Management (`/forum/user/devices`)
- `GET /device-list` - Get active login devices
- `DELETE /{deviceId}/revoke` - Revoke login for a specific device
- `DELETE /` - Revoke logins on all other devices

### 📰 Posts (`/forum/posts`)
- `POST /create` - Create a new post
- `GET /{postId}` - View post details
- `GET /` - Fetch posts filtered by userId, keyword, or tag
- `GET /all` - Fetch all posts (paginated using Spring Pageable)
- `GET /newsfeed` - Fetch feed feed (cursor-paginated)
- `GET /search` - Advanced search posts
- `PATCH /{id}/update` - Edit post content
- `PATCH /{id}/soft-delete` - Soft delete post
- `DELETE /{id}` - Hard delete post from DB
- `POST /{postId}/media` - Upload multiple image files to post (`MultipartFile`)
- `DELETE /{postId}/media/{mediaId}` - Delete post media attachment

### 🗳️ Post Voting (`/forum/posts`)
- `POST /{post_id}/vote` - Upvote or downvote post (`VoteType`)
- `GET /{post_id}/vote` - Retrieve current voting status of a post

### 💬 Comments (`/forum/post/comment`)
- `POST /create` - Write a new comment or reply to an existing one
- `GET /{postId}/rootCommentWithCount` - Fetch root comments (cursor-paginated)
- `GET /{postId}/{parentId}/replies` - Fetch child comments (offset-paginated Pageable)
- `PATCH /{commentId}/update` - Edit comment content
- `PATCH /{commentId}` - Soft delete comment
- `DELETE /{commentId}` - Hard delete comment
- `GET /{commentId}/context` - Get flat list of thread ancestors leading to target comment

### 🗳️ Comment Voting (`/forum/post/comments`)
- `POST /{commentId}/vote` - Upvote or downvote comment

### 📁 Collections (`/forum/saved`)
- `POST /collections` - Create a new post saving collection
- `PATCH /collections` - Edit collection title
- `GET /collections` - List all collections
- `GET /collections/{id}` - View posts saved inside a specific collection
- `DELETE /collections` - Delete collection
- `POST /collections/saved` - Save a post to collection (`collectionId`, `postId`)
- `DELETE /collections/saved` - Remove post from collection
- `GET /collections/saved-search` - Search inside saved posts

### 👥 Follows (`/forum/user`)
- `POST /{followingId}/follow` - Follow user
- `DELETE /{followingId}/unfollow` - Unfollow user
- `GET /me/follower` - Get list of users following you
- `GET /me/following` - Get list of users you follow
- `GET /{userId}/follower/count` - Count followers of a user
- `GET /{userId}/following/count` - Count users a user follows
- `DELETE /me/follower/{followerId}/remove` - Remove a follower from list

### 👤 User Profiles (`/forum/users`)
- `GET /me` - Get detailed user profile information
- `GET /{id}` - View user profile by ID
- `GET /` / `GET /all` - Fetch user lists
- `PATCH /{id}/change-password` - Change account password
- `PATCH /{id}/update` - Update account profile details (display name, avatar, bio)
- `PATCH /{id}` - Soft delete/suspend user account
- `DELETE /{id}` - Hard delete user account from DB

### 🏷️ Tags (`/forum/tags`)
- `GET /` - List all hashtags in system

### 📤 Upload API (`/forum/upload`)
- `POST /avatar` - Upload account avatar image (`multipart/form-data`)
- `POST /post-media` - Upload multiple post attachments

### 💬 Chat Channels & Messages (`/forum/chats`)
- `GET /` - List chat rooms (paginated)
- `GET /{id}` - View chat room details
- `POST /` - Create a Direct Chat room
- `POST /groupChats` - Create a Group Chat room
- `PATCH /{id}` - Edit chat group name/avatar
- `POST /{id}/avatar` - Upload chat group avatar
- `DELETE /{id}` - Delete chat room
- `GET /{id}/messages` - Fetch paginated chat room message history
- `POST /{chatId}/messages` - Send text message
- `POST /{chatId}/media-messages` - Send message with attachments
- `PATCH /messages/{id}` - Edit message content
- `DELETE /messages/{id}` - Revoke/delete sent message

### 👥 Chat Members (`/forum/chats`)
- `GET /{id}/members` - Get list of members in group chat
- `POST /{id}/members` - Add new members to group
- `PATCH /{id}/members/{memId}` - Change member role (`memId`, `ChatRole`)
- `PATCH /{id}/read` - Mark all messages in room as read
- `PATCH /{id}/setting` - Update chat settings for current user
- `DELETE /{id}/members/{memId}` - Kick member from group chat
- `DELETE /{id}/members/me` - Leave group chat room

### 🔔 Notifications (`/forum/user`)
- `GET /me/notification` - List user notifications (paginated)
- `GET /me/notification/count` - Get count of unread notifications
- `PATCH /me/notification/markAllRead` - Mark all notifications as read
- `PATCH /me/notification/{id}` - Mark single notification as read
- `PATCH /me/notification/{id}/archive` - Archive notification
- `DELETE /me/notification/{id}` - Delete notification

---

## 📡 WebSocket STOMP Configurations
- **STOMP Endpoint**: `/ws` (with SockJS fallback)
- **Application Destination Prefix**: `/app`
- **User Destination Prefix**: `/user`
- **Simple Broker Destinations**: `/topic`, `/queue`
- **Supported WS Message Mappings**:
  - `/chat.typing` - Send/Receive typing status in chat rooms

---

## ⚙️ Local Development Setup

### System Prerequisites
- **Java JDK 17** or higher.
- **PostgreSQL** Server with a database named `forum_db` created.
- **Redis** Server running on default port `6379`.
- **Cloudinary** account and credentials.

### Configuration Properties
1. Copy the example properties template file:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
2. Open `src/main/resources/application.properties` and replace the placeholder credentials (such as Database username/password, JWT secret, Cloudinary credentials, Google OAuth keys, and SMTP Mail credentials) with your actual local configuration values.

### Build & Run
1. Build the project using Maven wrapper:
   ```bash
   ./mvnw clean install
   ```
2. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The API will be active and listening at `http://localhost:8080/forum`.
