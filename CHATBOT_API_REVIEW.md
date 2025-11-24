# Báo Cáo Kiểm Tra API Chatbot

## 📋 Tổng Quan

Hệ thống chatbot hiện tại có **2 phần chính**:
1. **Python Chatbot API** (KLTN_BE_Model) - Xử lý logic chatbot và tìm kiếm sản phẩm
2. **Java Spring Boot API** (KLTN_BE) - Quản lý lưu trữ chat messages trong database

---

## 🔍 Các API Hiện Có

### 1. Python Chatbot API (FastAPI) - Port 8000

#### POST `/chat`
- **Mô tả**: Endpoint chính để chat với chatbot
- **Request Body**:
  ```json
  {
    "user_id": "1",
    "message": "Tôi muốn tìm giày Nike",
    "k": 20
  }
  ```
- **Response**:
  ```json
  {
    "reply": "Dựa trên nhu cầu của bạn...",
    "product_ids": ["PROD001", "PROD002"],
    "top_products": [...],
    "follow_up": false
  }
  ```
- **Chức năng**:
  - Phân tích nhu cầu người dùng
  - Tìm kiếm sản phẩm phù hợp
  - Trả về phản hồi thông minh
  - Tự động lưu vào database (bảng `chat_messages`)

---

### 2. Java Spring Boot API - Port 8080

#### GET `/api/chat-messages/user/{userId}`
- **Mô tả**: Lấy danh sách chat messages của user
- **Response**: `List<ChatMessageDTO>`
- **Sắp xếp**: Theo `createdAt` DESC (mới nhất trước)

#### POST `/api/chat-messages`
- **Mô tả**: Tạo chat message mới
- **Request Body**:
  ```json
  {
    "userId": 1,
    "message": "Tôi muốn tìm giày Nike",
    "response": "Tôi tìm thấy các sản phẩm Nike cho bạn",
    "productIds": "PROD001,PROD002",
    "intent": "search_product",
    "context": "{\"category\": \"sneakers\"}"
  }
  ```
- **Response**: `ChatMessageDTO` (201 Created)

---

## ⚠️ Các Vấn Đề Phát Hiện

### 1. **Thiếu API trong Postman Collection**

**Vấn đề**: Postman collection chỉ có Java APIs, thiếu Python Chatbot API endpoint.

**Khuyến nghị**: Thêm Python Chatbot API vào Postman collection:
```json
{
  "name": "Chatbot APIs",
  "item": [
    {
      "name": "POST Chat with Bot",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"user_id\": \"1\",\n  \"message\": \"Tôi muốn tìm giày Nike\",\n  \"k\": 20\n}"
        },
        "url": {
          "raw": "http://localhost:8000/chat",
          "host": ["localhost"],
          "port": "8000",
          "path": ["chat"]
        }
      }
    }
  ]
}
```

---

### 2. **Không Đồng Bộ Giữa 2 Hệ Thống**

**Vấn đề**: 
- Python chatbot tự động lưu vào database của nó
- Java API cũng có bảng `chat_messages` riêng
- Có thể dẫn đến dữ liệu không đồng bộ

**Khuyến nghị**:
- **Option 1**: Python chatbot gọi Java API để lưu thay vì lưu trực tiếp
- **Option 2**: Java API đọc từ cùng database mà Python chatbot đang dùng
- **Option 3**: Tạo service để đồng bộ dữ liệu giữa 2 hệ thống

---

### 3. **DTO Có Field Không Được Sử Dụng**

**Vấn đề**: `ChatMessageDTO` có field `productIdList` nhưng không được set trong `convertToDTO()`.

**Code hiện tại**:
```java
private ChatMessageDTO convertToDTO(ChatMessage message) {
    // ... thiếu setProductIdList()
}
```

**Khuyến nghị**: 
- Nếu `productIds` là JSON string, parse thành List trong DTO
- Hoặc xóa field `productIdList` nếu không cần

---

### 4. **Thiếu Validation và Error Handling**

**Vấn đề**:
- Java API không validate `userId` có tồn tại không
- Không có error handling khi user không tồn tại
- Không có pagination cho GET messages

**Khuyến nghị**:
```java
@GetMapping("/user/{userId}")
public ResponseEntity<?> getUserChatMessages(
    @PathVariable Long userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    // Validate user exists
    // Add pagination
    // Better error handling
}
```

---

### 5. **Thiếu API Endpoints Hữu Ích**

**Các API nên thêm**:
- `DELETE /api/chat-messages/{id}` - Xóa một message
- `DELETE /api/chat-messages/user/{userId}` - Xóa tất cả messages của user
- `GET /api/chat-messages/{id}` - Lấy chi tiết một message
- `GET /api/chat-messages/user/{userId}/stats` - Thống kê chat của user

---

### 6. **Inconsistency trong ProductIds Format**

**Vấn đề**:
- Python chatbot lưu `product_ids` dưới dạng JSON array: `["PROD001", "PROD002"]`
- Java API example trong Postman dùng string: `"PROD001,PROD002"`
- Entity Java dùng `String` type cho `productIds` (JSONB)

**Khuyến nghị**: 
- Thống nhất format: dùng JSON array
- Update Postman example để match với format thực tế

---

### 7. **Thiếu API Documentation**

**Vấn đề**: 
- Java API không có Swagger/OpenAPI documentation
- Python API có FastAPI docs nhưng chưa đầy đủ

**Khuyến nghị**:
- Thêm Swagger cho Java API
- Bổ sung description chi tiết cho các endpoints

---

## ✅ Điểm Tốt

1. ✅ Python chatbot có logic xử lý phức tạp và thông minh
2. ✅ Java API có cấu trúc rõ ràng với DTO pattern
3. ✅ Có lưu trữ context và intent để phân tích sau
4. ✅ Hỗ trợ multi-turn conversation

---

## 📝 Khuyến Nghị Ưu Tiên

### Priority 1 (Quan trọng)
1. **Thêm Python Chatbot API vào Postman collection**
2. **Đồng bộ dữ liệu giữa Python và Java** (chọn 1 trong 3 options)
3. **Fix productIds format inconsistency**

### Priority 2 (Nên có)
4. **Thêm validation và error handling**
5. **Thêm pagination cho GET messages**
6. **Fix productIdList field trong DTO**

### Priority 3 (Có thể cải thiện)
7. **Thêm các API endpoints bổ sung** (DELETE, stats)
8. **Thêm Swagger documentation**

---

## 🔧 Code Examples để Fix

### Fix 1: Thêm Pagination cho GET Messages

```java
@GetMapping("/user/{userId}")
public ResponseEntity<Page<ChatMessageDTO>> getUserChatMessages(
    @PathVariable Long userId,
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    Page<ChatMessage> messages = chatMessageRepository.findByUserId(userId, pageable);
    Page<ChatMessageDTO> dtos = messages.map(this::convertToDTO);
    return ResponseEntity.ok(dtos);
}
```

### Fix 2: Parse productIds thành List

```java
private ChatMessageDTO convertToDTO(ChatMessage message) {
    ChatMessageDTO dto = new ChatMessageDTO();
    // ... existing code ...
    
    // Parse productIds JSON string to List
    if (message.getProductIds() != null && !message.getProductIds().isEmpty()) {
        try {
            List<String> productIdList = objectMapper.readValue(
                message.getProductIds(), 
                new TypeReference<List<String>>() {}
            );
            dto.setProductIdList(productIdList);
        } catch (Exception e) {
            logger.warn("Failed to parse productIds: {}", e.getMessage());
        }
    }
    
    return dto;
}
```

### Fix 3: Thêm Validation

```java
@PostMapping
public ResponseEntity<?> createChatMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
    // Validate required fields
    if (chatMessageDTO.getMessage() == null || chatMessageDTO.getMessage().trim().isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Message is required"));
    }
    
    // Validate user exists if userId provided
    if (chatMessageDTO.getUserId() != null) {
        Optional<User> user = userRepository.findByUserId(chatMessageDTO.getUserId());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "User not found"));
        }
    }
    
    // ... rest of code
}
```

---

## 📊 So Sánh 2 Hệ Thống

| Tính năng | Python API | Java API |
|-----------|-----------|----------|
| Chat logic | ✅ Có | ❌ Không |
| Lưu database | ✅ Tự động | ✅ Manual |
| Pagination | ❌ Không | ❌ Không |
| Validation | ⚠️ Cơ bản | ⚠️ Cơ bản |
| Error handling | ✅ Có | ⚠️ Cơ bản |
| Documentation | ✅ FastAPI docs | ❌ Chưa có |

---

## 🎯 Kết Luận

Hệ thống chatbot có cấu trúc tốt nhưng cần:
1. **Đồng bộ dữ liệu** giữa Python và Java
2. **Bổ sung API endpoints** còn thiếu
3. **Cải thiện validation và error handling**
4. **Cập nhật Postman collection** đầy đủ

Sau khi fix các vấn đề trên, hệ thống sẽ hoàn thiện và dễ maintain hơn.

