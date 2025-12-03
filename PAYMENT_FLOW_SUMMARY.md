# Tóm tắt Flow API từ đầu đến Payment

## 📋 Tổng quan Flow

Flow thanh toán được chia thành các bước chính:
1. **Quản lý Cart** (Giỏ hàng)
2. **Tạo Order** (Đơn hàng)
3. **Tạo Payment Link** (Liên kết thanh toán)
4. **Xử lý Payment** (Thanh toán)
5. **Webhook & Status** (Cập nhật trạng thái)

---

## 🔄 Chi tiết Flow

### **BƯỚC 1: Quản lý Cart (Giỏ hàng)**

#### 1.1. Lấy/Create Cart
```
GET /api/carts
Authorization: Bearer {access_token}
```
- Lấy cart đang active của user
- Nếu chưa có → tự động tạo cart mới với status = "ACTIVE"
- **Response**: CartDTO với danh sách items

#### 1.2. Thêm sản phẩm vào Cart
```
POST /api/carts/{cartId}/items
Authorization: Bearer {access_token}
Body: {
  "productId": "PROD001",
  "quantity": 2,
  "unitPrice": 2500000  // Optional
}
```
- Thêm sản phẩm vào cart
- Nếu sản phẩm đã có → cộng dồn quantity
- **Response**: CartItemDTO

#### 1.3. Cập nhật/Clear Cart
```
PATCH /api/carts/{cartId}/items/{itemId}  // Update quantity
DELETE /api/carts/{cartId}/items/{itemId}  // Remove item
DELETE /api/carts/{cartId}/items  // Clear all items
```

**Lưu ý**: Frontend có thể tự tính `total` từ danh sách items (không cần backend tính)

---

### **BƯỚC 2: Tạo Order (Đơn hàng)**

#### 2.1. Create Order
```
POST /api/orders
Authorization: Bearer {access_token}
Body: {
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "paymentMethod": "PAYOS" | "COD",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "unitPrice": 2500000
    },
    {
      "productId": "PROD002",
      "quantity": 1,
      "unitPrice": 3000000
    }
  ]
}
```

**Xử lý trong OrderService:**
1. Lấy user từ JWT token
2. Tạo Order entity với:
   - `status` = "PENDING"
   - `paymentMethod` = "PAYOS" hoặc "COD"
   - `shippingAddress`
3. Tạo OrderItem cho mỗi item trong request
4. Tính `totalAmount` = tổng (unitPrice × quantity)
5. Lưu vào database
6. **Response**: OrderDTO với `id`, `status`, `totalAmount`, `items`, etc.

**Lưu ý:**
- Order được tạo với status = "PENDING"
- Payment method phải được set ngay từ đầu
- Nếu paymentMethod = "COD" → không cần tạo payment link

---

### **BƯỚC 3: Tạo Payment Link (Chỉ cho PAYOS)**

#### 3.1. Create Payment Link
```
POST /api/payments/create-link
Authorization: Bearer {access_token}
Body: {
  "orderId": 51,
  "returnUrl": "https://kltn-be-nx3d.onrender.com/payment/success",
  "cancelUrl": "https://kltn-be-nx3d.onrender.com/payment/cancel"
}
```

**Xử lý trong PaymentController:**
1. **Validate Order:**
   - Kiểm tra order tồn tại
   - Kiểm tra `paymentMethod` = "PAYOS"
   - Nếu không phải PAYOS → trả về error

2. **Tạo PaymentLinkRequest:**
   - Lấy thông tin từ Order (amount, items, description)
   - Map OrderItem → PaymentItem
   - Tạo description: "Thanh toán đơn hàng #{orderId}"

3. **Gọi PayOSService.createPaymentLink():**
   - Tạo payload với các trường:
     - `orderCode`: orderId (int)
     - `amount`: totalAmount (int)
     - `description`
     - `returnUrl`
     - `cancelUrl`
     - `items`: array of items (nếu có)
   
   - **Tạo Signature:**
     - Tạo `signatureData` Map (TreeMap để sort alphabetically)
     - Các trường: `amount`, `cancelUrl`, `description`, `orderCode`, `returnUrl`
     - Nếu có items → serialize thành JSON string và thêm vào signatureData
     - **URL encode** tất cả giá trị
     - Tạo chuỗi: `key1=encodedValue1&key2=encodedValue2&...`
     - HMAC-SHA256 với `checksumKey`
     - Base64 encode → signature
   
   - Gửi request đến PayOS API: `POST /v2/payment-requests`
   - Headers:
     - `x-client-id`: PAYOS_CLIENT_ID
     - `x-api-key`: PAYOS_API_KEY
     - `Content-Type`: application/json

4. **Cập nhật Order:**
   - Nếu PayOS trả về success (code = 0):
     - `updatePaymentInfo()`:
       - `paymentLinkId` = response.data.paymentLinkId
       - `paymentStatus` = response.data.status
       - `paymentCode` = response.data.orderCode (string)

5. **Response:**
```json
{
  "checkoutUrl": "https://pay.payos.vn/web/...",
  "paymentLinkId": "abc123xyz",
  "orderCode": 123456,
  "qrCode": "data:image/png;base64,..."
}
```

**Lỗi có thể xảy ra:**
- Order not found
- Payment method không phải PAYOS
- Signature không hợp lệ (đã fix bằng URL encoding)
- PayOS API error

---

### **BƯỚC 4: Xử lý Payment**

#### 4.1. User thanh toán
- User click vào `checkoutUrl` hoặc scan QR code
- Redirect đến PayOS payment page
- User thực hiện thanh toán

#### 4.2. PayOS redirect về returnUrl/cancelUrl
- **Success**: Redirect về `returnUrl`
- **Cancel**: Redirect về `cancelUrl`

---

### **BƯỚC 5: Webhook & Status Update**

#### 5.1. PayOS Webhook (Tự động)
```
POST /api/payments/webhook
Body: {
  "code": "00",
  "desc": "Thành công",
  "data": {
    "orderCode": "123456",
    "amount": 8000000,
    "description": "Thanh toán đơn hàng #51",
    "accountNumber": "970415",
    "reference": "REF123456",
    "transactionDateTime": "2024-01-15T10:30:00Z",
    "currency": "VND",
    "paymentLinkId": "abc123xyz",
    "code": "00",
    "desc": "Thành công"
  },
  "signature": "abc123xyz..."
}
```

**Xử lý trong PaymentController:**
1. **Verify Signature:**
   - `payOSService.verifyWebhookSignature(payload)`
   - Build dataMap từ payload.data (TreeMap, sorted)
   - Generate signature tương tự như create link
   - So sánh với payload.signature
   - Nếu không match → return 401 Unauthorized

2. **Xử lý Payment:**
   - Nếu `data.code` = "00" (Thành công):
     - `orderService.completePayment(orderCode, reference)`
     - Tìm order theo `paymentCode` = orderCode
     - Update:
       - `paymentStatus` = "PAID"
       - `status` = "CONFIRMED"
   - Response: `{"code": "00", "desc": "Success"}`

#### 5.2. Check Payment Status (Manual)
```
GET /api/payments/status/{orderCode}
Authorization: Bearer {access_token}
```
- Gọi PayOS API để lấy trạng thái payment trực tiếp từ PayOS
- Response: orderCode, status, amount, description

**Lưu ý**: Để lấy payment status từ orderId, frontend nên sử dụng:
```
GET /api/orders/{orderId}
```
- Response đã bao gồm `paymentStatus` và `status` của order
- Không cần API riêng để check payment status từ orderId

---

## 📊 Order Status Flow

```
PENDING (Tạo order)
    ↓
[PAYOS] → Tạo payment link → paymentStatus = null
    ↓
[User thanh toán]
    ↓
[Webhook] → paymentStatus = "PAID" → status = "CONFIRMED"
    ↓
[Admin xử lý] → status = "SHIPPING" → "DELIVERED" → "COMPLETED"
```

**Payment Status:**
- `null`: Chưa tạo payment link
- `PENDING`: Đã tạo payment link, chưa thanh toán
- `PAID`: Đã thanh toán thành công

**Order Status:**
- `PENDING`: Đơn hàng mới tạo
- `CONFIRMED`: Đã thanh toán (tự động khi paymentStatus = "PAID")
- `SHIPPING`: Đang vận chuyển
- `DELIVERED`: Đã giao hàng
- `COMPLETED`: Hoàn thành
- `CANCELLED`: Đã hủy

---

## 🔐 Authentication

Tất cả API (trừ webhook) yêu cầu:
```
Authorization: Bearer {access_token}
```

**Lấy access_token:**
```
POST /api/auth/login
Body: {
  "username": "...",
  "password": "..."
}
```

---

## 📝 Lưu ý quan trọng

1. **Order phải được tạo trước** khi tạo payment link
2. **Payment method** phải được set khi tạo order
3. **Signature** phải URL-encode tất cả giá trị (đã fix)
4. **Webhook** không cần authentication (PayOS gọi trực tiếp)
5. **Order status** tự động update khi payment thành công qua webhook
6. **COD orders** không cần tạo payment link

---

## 🛠️ Các API Endpoints

### Cart APIs
- `GET /api/carts` - Lấy/Create cart
- `POST /api/carts/items` - Thêm item vào cart active
- `GET /api/carts/{cartId}/items` - Lấy danh sách items (không có meta.total)
- `PATCH /api/carts/{cartId}/items/{itemId}` - Update quantity
- `DELETE /api/carts/{cartId}/items/{itemId}` - Remove item
- `DELETE /api/carts/{cartId}/items` - Clear all items

### Order APIs
- `POST /api/orders` - Tạo order
- `GET /api/orders` - Lấy danh sách orders
- `GET /api/orders/{orderId}` - Lấy order by ID

### Payment APIs
- `POST /api/payments/create-link` - Tạo payment link
- `GET /api/payments/status/{orderCode}` - Check payment status by orderCode (từ PayOS)
- `POST /api/payments/webhook` - PayOS webhook (no auth)

**Lưu ý**: Để check payment status từ orderId, sử dụng `GET /api/orders/{orderId}` (đã có paymentStatus trong response)

---

## 🔄 Flow Diagram

```
User
  ↓
[1] Add to Cart → Cart API
  ↓
[2] Create Order → Order API (paymentMethod = "PAYOS")
  ↓
[3] Create Payment Link → Payment API
  ↓
[4] Redirect to PayOS → User thanh toán
  ↓
[5] PayOS Webhook → Backend (auto update order)
  ↓
[6] Order Status = "CONFIRMED"
```

---

## ✅ Checklist khi test

- [ ] Cart được tạo và quản lý đúng
- [ ] Order được tạo với paymentMethod = "PAYOS"
- [ ] Payment link được tạo thành công (không lỗi signature)
- [ ] CheckoutUrl và QR code được trả về
- [ ] Webhook được gọi khi thanh toán thành công
- [ ] Order status tự động update thành "CONFIRMED"
- [ ] Payment status = "PAID"
- [ ] Có thể check payment status bằng API

