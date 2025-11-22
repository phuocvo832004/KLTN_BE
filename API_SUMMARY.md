# Product API Summary

Base URL: `/api/products`

Tất cả các API đều hỗ trợ pagination và sorting.

---

## 📋 Query Parameters chung (Pagination & Sorting)

Các query parameters sau có thể được sử dụng cho các GET endpoints (trừ `/api/products/{id}`):

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | int | No | `0` | Số trang (bắt đầu từ 0). Nếu `page < 0` hoặc `size <= 0`, API sẽ trả về danh sách không phân trang (backward compatibility) |
| `size` | int | No | `24` | Số lượng items mỗi trang |
| `sortBy` | String | No | `id` | Field để sort (ví dụ: `id`, `title`, `price`, `createdAt`, `averageRating`) |
| `sortDir` | String | No | `asc` | Hướng sort: `asc` hoặc `desc` |

### Response Format (với pagination)

```json
{
  "content": [...],           // Array of ProductDTO
  "page": 0,                  // Current page number
  "size": 24,                 // Page size
  "totalElements": 100,        // Total number of items
  "totalPages": 5,            // Total number of pages
  "first": true,              // Is first page?
  "last": false               // Is last page?
}
```

### Response Format (không pagination - backward compatibility)

Khi `page < 0` hoặc `size <= 0`, API trả về:
```json
[...]  // Array of ProductDTO
```

---

## 🔍 API Endpoints

### 1. Get All Products
**GET** `/api/products`

Lấy tất cả sản phẩm với pagination.

**Query Parameters:**
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products?page=0&size=12&sortBy=price&sortDir=desc
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 2. Get Product By ID
**GET** `/api/products/{id}`

Lấy chi tiết một sản phẩm theo ID, bao gồm reviews với pagination.

**Path Parameters:**
- `id` (required) - Product ID

**Query Parameters (cho reviews):**
- `reviewPage` (optional, default: `-1`) - Nếu `-1`, trả về tất cả reviews
- `reviewSize` (optional, default: `10`) - Số lượng reviews mỗi trang
- `reviewSortBy` (optional, default: `reviewDate`) - Field để sort reviews
- `reviewSortDir` (optional, default: `desc`) - Hướng sort reviews

**Example:**
```
GET /api/products/123?reviewPage=0&reviewSize=5&reviewSortBy=reviewDate&reviewSortDir=desc
```

**Response:** `ProductDTO` (404 nếu không tìm thấy)

---

### 3. Search Products
**GET** `/api/products/search`

Tìm kiếm sản phẩm theo title.

**Query Parameters:**
- `title` (required) - Từ khóa tìm kiếm
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products/search?title=nike&page=0&size=12&sortBy=price&sortDir=asc
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 4. Get Products By Category
**GET** `/api/products/category/{category}`

Lấy sản phẩm theo category.

**Path Parameters:**
- `category` (required) - Tên category

**Query Parameters:**
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products/category/shoes?page=0&size=12
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 5. Get Special Offers
**GET** `/api/products/special-offers`

Lấy danh sách sản phẩm special offers (type = 0 trong season_categories).

**Query Parameters:**
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products/special-offers?page=0&size=8&sortBy=sale&sortDir=desc
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 6. Get New Arrivals
**GET** `/api/products/new-arrivals`

Lấy danh sách sản phẩm new arrivals (type = 1 trong season_categories).

**Query Parameters:**
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products/new-arrivals?page=0&size=8&sortBy=createdAt&sortDir=desc
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 7. Get Products By Season Type
**GET** `/api/products/season/{type}`

Lấy sản phẩm theo season type (generic endpoint).

**Path Parameters:**
- `type` (required) - Season type:
  - `0` = Special Offers
  - `1` = New Arrivals
  - `2` = Other type 1
  - `3` = Other type 2

**Query Parameters:**
- `page` (optional, default: `0`)
- `size` (optional, default: `24`)
- `sortBy` (optional, default: `id`)
- `sortDir` (optional, default: `asc`)

**Example:**
```
GET /api/products/season/0?page=0&size=12
GET /api/products/season/1?page=0&size=12
```

**Response:** `PageResponse<ProductDTO>` hoặc `List<ProductDTO>` (nếu không pagination)

---

### 8. Create Product
**POST** `/api/products`

Tạo sản phẩm mới.

**Request Body:** `ProductDTO`

**Example:**
```json
POST /api/products
Content-Type: application/json

{
  "id": "product-123",
  "title": "Nike Air Force 1",
  "description": "...",
  "price": 99.99,
  "sale": 79.99,
  "imgUrl": "...",
  ...
}
```

**Response:** `ProductDTO` (201 Created) hoặc 400 Bad Request

---

### 9. Update Product
**PUT** `/api/products/{id}`

Cập nhật thông tin sản phẩm.

**Path Parameters:**
- `id` (required) - Product ID

**Request Body:** `ProductDTO`

**Response:** `ProductDTO` (200 OK) hoặc 404 Not Found

---

### 10. Delete Product
**DELETE** `/api/products/{id}`

Xóa sản phẩm.

**Path Parameters:**
- `id` (required) - Product ID

**Response:** 204 No Content

---

## 📝 ProductDTO Structure

```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "price": 99.99,
  "sale": 79.99,              // NEW: Sale price or discount percentage
  "imurl": "string",
  "imgUrl": "string",
  "categories": ["string"],
  "specs": "json string",
  "averageRating": 4.5,
  "rating": 4.5,              // Alias for averageRating
  "relatedProducts": ["string"],
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "categoryList": ["string"],
  "specList": [
    {
      "specKey": "string",
      "specValue": "string"
    }
  ],
  "reviews": [...]            // Only included in getProductById
}
```

---

## 🎯 Quick Reference

### Trang chủ - Special Offers
```
GET /api/products/special-offers?page=0&size=8
```

### Trang chủ - New Arrivals
```
GET /api/products/new-arrivals?page=0&size=8
```

### Danh sách sản phẩm
```
GET /api/products?page=0&size=24&sortBy=price&sortDir=asc
```

### Tìm kiếm
```
GET /api/products/search?title=nike&page=0&size=24
```

### Theo category
```
GET /api/products/category/shoes?page=0&size=24
```

---

## ⚠️ Lưu ý

1. **Backward Compatibility**: Tất cả các GET endpoints (trừ `/api/products/{id}`) vẫn hỗ trợ trả về danh sách không phân trang nếu `page < 0` hoặc `size <= 0`.

2. **Default Values**: 
   - `page = 0` (trang đầu tiên)
   - `size = 24` (24 items mỗi trang)
   - `sortBy = id`
   - `sortDir = asc`

3. **Season Categories**: Để sử dụng special offers và new arrivals, cần insert dữ liệu vào bảng `season_categories` với:
   - `type = 0` cho special offers
   - `type = 1` cho new arrivals
   - `product_id` là ID của sản phẩm

4. **Sale Field**: Field `sale` trong Product có thể dùng để lưu giá sale hoặc phần trăm giảm giá tùy theo logic business của bạn.

