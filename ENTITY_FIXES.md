# Entity Fixes - Sửa lỗi @GeneratedValue

## Vấn đề

JPA không cho phép sử dụng `@GeneratedValue` trên các field không phải là `@Id`. Lỗi xảy ra với các entity có cả primary key và một field `id` khác được generate tự động trong database.

## Các entity đã sửa

### 1. Review Entity
**Vấn đề**: Field `id` có `@GeneratedValue` nhưng không phải là `@Id` (primary key là `review_id`)

**Giải pháp**: 
- Xóa `@GeneratedValue` khỏi field `id`
- Đánh dấu field là `insertable = false, updatable = false` để JPA không quản lý field này
- Database vẫn tự động generate giá trị cho field `id`

```java
// Trước
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id")
private Long id;

// Sau
// Note: 'id' column exists in DB but is not the primary key
// JPA will not manage this field since it's not @Id
@Column(name = "id", insertable = false, updatable = false)
private Long id;
```

### 2. User Entity
**Vấn đề**: Field `id` có `@GeneratedValue` nhưng không phải là `@Id` (primary key là `user_id`)

**Giải pháp**: Tương tự như Review entity

### 3. Interaction Entity
**Vấn đề**: 
- Field `id` có `@GeneratedValue` nhưng không phải là `@Id` (primary key là `interaction_id`)
- Sử dụng `StringArrayConverter` không phù hợp cho PostgreSQL array type

**Giải pháp**: 
- Xóa `@GeneratedValue` khỏi field `id` và đánh dấu là read-only
- Thay `@Convert(converter = StringArrayConverter.class)` bằng `@JdbcTypeCode(SqlTypes.ARRAY)` để Hibernate tự xử lý PostgreSQL array

```java
// Trước
@Column(name = "recommended_ids", columnDefinition = "_varchar")
@Convert(converter = StringArrayConverter.class)
private String[] recommendedIds;

// Sau
@Column(name = "recommended_ids", columnDefinition = "_varchar")
@JdbcTypeCode(SqlTypes.ARRAY)
private String[] recommendedIds;
```

### 4. Product Entity
**Vấn đề**: PostgreSQL array types (`_varchar`) cần annotation đặc biệt để Hibernate xử lý đúng

**Giải pháp**: Thêm `@JdbcTypeCode(SqlTypes.ARRAY)` cho các field array:
- `categories`
- `relatedProducts`

## Nguyên tắc

1. **Chỉ sử dụng `@GeneratedValue` với `@Id`**: Nếu một field không phải là primary key, không được dùng `@GeneratedValue`

2. **Read-only fields**: Nếu database tự động generate giá trị cho một field không phải primary key, đánh dấu field đó là `insertable = false, updatable = false`

3. **PostgreSQL Arrays**: Sử dụng `@JdbcTypeCode(SqlTypes.ARRAY)` thay vì custom converter cho PostgreSQL array types

## Kiểm tra

Sau khi sửa, backend sẽ khởi động thành công mà không có lỗi:
```
Failed to initialize JPA EntityManagerFactory: Property '...id' is annotated @GeneratedValue but is not part of an identifier
```

## Lưu ý

- Các field `id` (không phải primary key) vẫn tồn tại trong database và được generate tự động
- JPA sẽ đọc giá trị từ database nhưng không ghi vào
- Nếu cần sử dụng giá trị `id` này, có thể đọc sau khi entity được persist

