# Filter API Guide

## Overview
API Filter tổng hợp cho phép lọc sản phẩm theo nhiều tiêu chí khác nhau trong một request duy nhất.

## Endpoint

```
POST /api/products/filter
```

## Request Parameters

### Query Parameters (Pagination & Sorting)
- `page` (optional, default: 0) - Số trang
- `size` (optional, default: 24) - Số lượng sản phẩm mỗi trang
- `sortBy` (optional, default: "id") - Trường để sắp xếp (id, price, averageRating, createdAt, etc.)
- `sortDir` (optional, default: "asc") - Hướng sắp xếp (asc hoặc desc)

### Request Body (Filter Criteria)

```json
{
  "keyword": "nike",
  "categories": ["Sneakers", "Running Shoes"],
  "minPrice": 50,
  "maxPrice": 200,
  "minRating": 4.0,
  "seasonType": 0,
  "hasDiscount": true
}
```

#### Filter Fields:

- **keyword** (String, optional): Tìm kiếm trong title và description
- **categories** (Array<String>, optional): Danh sách categories cần lọc
- **minPrice** (Number, optional): Giá tối thiểu
- **maxPrice** (Number, optional): Giá tối đa
- **minRating** (Number, optional): Đánh giá tối thiểu (0-5)
- **seasonType** (Integer, optional): Loại mùa
  - `0` = Special Offers / Trending
  - `1` = New Arrivals
  - `2` = Winter
  - `3` = Summer
- **hasDiscount** (Boolean, optional): Chỉ lấy sản phẩm có giảm giá

## Response Format

### Paginated Response
```json
{
  "content": [
    {
      "id": "PROD001",
      "title": "Nike Air Max",
      "description": "...",
      "price": 150.00,
      "sale": 120.00,
      "categories": ["Sneakers", "Running Shoes"],
      "averageRating": 4.5,
      "specs": "...",
      "imurl": "...",
      "imgUrl": "..."
    }
  ],
  "pageNumber": 0,
  "pageSize": 24,
  "totalElements": 45,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

### Array Response (when page < 0 or size <= 0)
```json
[
  {
    "id": "PROD001",
    "title": "Nike Air Max",
    ...
  }
]
```

## Usage Examples

### Example 1: Filter by Price Range
```bash
curl -X POST "http://localhost:8080/api/products/filter?page=0&size=24&sortBy=price&sortDir=asc" \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 50,
    "maxPrice": 150
  }'
```

### Example 2: Filter by Categories and Rating
```bash
curl -X POST "http://localhost:8080/api/products/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "categories": ["Sneakers", "Running Shoes"],
    "minRating": 4.0
  }'
```

### Example 3: Search with Multiple Filters
```bash
curl -X POST "http://localhost:8080/api/products/filter?sortBy=averageRating&sortDir=desc" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "nike",
    "categories": ["Sneakers"],
    "minPrice": 100,
    "maxPrice": 300,
    "minRating": 4.5,
    "hasDiscount": true
  }'
```

### Example 4: Get Only Sale Items
```bash
curl -X POST "http://localhost:8080/api/products/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "hasDiscount": true
  }'
```

### Example 5: Get New Arrivals in Price Range
```bash
curl -X POST "http://localhost:8080/api/products/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "seasonType": 1,
    "minPrice": 80,
    "maxPrice": 200
  }'
```

## Frontend Integration

### Using the Filter API in Frontend

```typescript
import AxiosAPI from '@/lib/axios';

const filterProducts = async () => {
  const filterRequest = {
    keyword: "nike",
    categories: ["Sneakers"],
    minPrice: 50,
    maxPrice: 200,
    minRating: 4.0,
    hasDiscount: true
  };

  const response = await AxiosAPI.post(
    '/api/products/filter?page=0&size=24&sortBy=price&sortDir=asc',
    filterRequest
  );

  const products = response.data?.data?.content || response.data?.data;
  return products;
};
```

## Notes

- Tất cả các filter fields đều optional
- Có thể kết hợp bất kỳ filter nào với nhau
- Kết quả được distinct để tránh duplicate khi join nhiều bảng
- Empty request body `{}` sẽ trả về tất cả sản phẩm
- Pagination chỉ hoạt động khi `page >= 0` và `size > 0`

## Related APIs

- `GET /api/products/search?title={keyword}` - Simple search by title only
- `GET /api/products/category/{category}` - Filter by single category
- `GET /api/products/popular?minRating={rating}` - Filter by rating only
- `GET /api/products/special-offers` - Get special offers
- `GET /api/products/new-arrivals` - Get new arrivals

