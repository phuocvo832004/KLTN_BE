# KLTN Backend API

Backend Spring Boot application cho hệ thống e-commerce.

## Yêu cầu

- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Cấu hình

1. Cập nhật file `src/main/resources/application.properties`:
   - Thay đổi `spring.datasource.url` với thông tin database của bạn
   - Thay đổi `spring.datasource.username` và `spring.datasource.password`

2. Đảm bảo database đã được tạo và các bảng đã được tạo theo schema SQL được cung cấp.

## Chạy ứng dụng

```bash
# Sử dụng Maven wrapper
./mvnw spring-boot:run

# Hoặc sử dụng Maven
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại `http://localhost:8080`

## API Endpoints

### Products
- `GET /api/products` - Lấy tất cả sản phẩm
- `GET /api/products/{id}` - Lấy sản phẩm theo ID
- `GET /api/products/search?title={title}` - Tìm kiếm sản phẩm
- `GET /api/products/category/{category}` - Lấy sản phẩm theo category
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/products/{id}` - Xóa sản phẩm

### Cart
- `GET /api/carts/user/{userId}` - Lấy giỏ hàng active của user
- `POST /api/carts/user/{userId}/items` - Thêm sản phẩm vào giỏ hàng
- `GET /api/carts/{cartId}/items` - Lấy các items trong giỏ hàng
- `PUT /api/carts/items/{itemId}?quantity={quantity}` - Cập nhật số lượng
- `DELETE /api/carts/items/{itemId}` - Xóa item khỏi giỏ hàng
- `DELETE /api/carts/{cartId}/items` - Xóa tất cả items
- `POST /api/carts/{cartId}/checkout` - Checkout giỏ hàng

### Orders
- `POST /api/orders/user/{userId}` - Tạo đơn hàng mới
- `GET /api/orders/user/{userId}` - Lấy đơn hàng của user
- `GET /api/orders/{orderId}` - Lấy đơn hàng theo ID
- `PUT /api/orders/{orderId}/status?status={status}` - Cập nhật trạng thái đơn hàng

### Reviews
- `GET /api/reviews/product/{productId}` - Lấy reviews của sản phẩm
- `POST /api/reviews` - Tạo review mới
- `DELETE /api/reviews/{reviewId}` - Xóa review

### Users
- `GET /api/users` - Lấy tất cả users
- `GET /api/users/{userId}` - Lấy user theo ID
- `GET /api/users/username/{username}` - Lấy user theo username
- `POST /api/users` - Tạo user mới
- `PUT /api/users/{userId}` - Cập nhật user
- `DELETE /api/users/{userId}` - Xóa user

### Chat Messages
- `GET /api/chat-messages/user/{userId}` - Lấy chat messages của user
- `POST /api/chat-messages` - Tạo chat message mới

## Cấu trúc dự án

```
src/main/java/com/fourj/kltn_be/
├── entity/          # JPA Entities
├── repository/       # JPA Repositories
├── dto/             # Data Transfer Objects
├── service/         # Business Logic
├── controller/      # REST Controllers
└── config/          # Configuration classes
```

## Lưu ý

- Backend hiện tại không có authentication/authorization. Cần thêm Spring Security nếu cần.
- CORS đã được cấu hình để cho phép tất cả origins. Nên hạn chế trong production.

