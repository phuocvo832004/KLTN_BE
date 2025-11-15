# Query Fix - Sửa lỗi JPQL với PostgreSQL Array

## Vấn đề

### Lỗi gốc
```
Non-boolean expression used in predicate context: p.categories
UnsatisfiedDependencyException: Error creating bean 'cartService'
```

### Nguyên nhân

1. **Query JPQL không hợp lệ với Hibernate 6+**:
   ```java
   @Query("SELECT p FROM Product p WHERE p.categories IS NOT NULL AND :category = ANY(p.categories)")
   ```
   
2. **Vấn đề kỹ thuật**:
   - `p.categories` là PostgreSQL array (`String[]`) được map với `@JdbcTypeCode(SqlTypes.ARRAY)`
   - Hibernate 6+ không hỗ trợ cú pháp `ANY(...)` trong JPQL với array types
   - JPQL không thể xử lý PostgreSQL array operators trực tiếp

3. **Cascade lỗi**:
   - ProductRepository không khởi tạo được → CartService lỗi → CartController lỗi
   - Ứng dụng không thể start

## Giải pháp

### Option 1: Query qua Relationship (Recommended)

Sử dụng bảng `product_categories` thay vì array column:

```java
@Query("SELECT DISTINCT p FROM Product p JOIN p.productCategories pc WHERE pc.category = :category")
List<Product> findByCategory(@Param("category") String category);
```

**Ưu điểm**:
- JPQL hợp lệ, Hibernate hỗ trợ tốt
- Type-safe, dễ maintain
- Phù hợp với database normalization

**Nhược điểm**:
- Cần dữ liệu trong bảng `product_categories`
- Nếu dữ liệu chỉ có trong array column thì không hoạt động

### Option 2: Native Query với PostgreSQL Array

Query trực tiếp từ array column:

```java
@Query(value = "SELECT * FROM products WHERE categories IS NOT NULL AND :category = ANY(categories)", nativeQuery = true)
List<Product> findByCategoryFromArray(@Param("category") String category);
```

**Ưu điểm**:
- Query trực tiếp từ array column
- Sử dụng PostgreSQL array operators (`ANY`)
- Không cần dữ liệu trong bảng relationship

**Nhược điểm**:
- Không portable (chỉ hoạt động với PostgreSQL)
- Không type-safe
- Phụ thuộc vào database-specific syntax

## Cách sử dụng

### Nếu dữ liệu có trong `product_categories` table:
```java
List<Product> products = productRepository.findByCategory("Electronics");
```

### Nếu chỉ có dữ liệu trong array column:
```java
List<Product> products = productRepository.findByCategoryFromArray("Electronics");
```

## Lưu ý

1. **Kiểm tra dữ liệu**: Xem dữ liệu category được lưu ở đâu (array column hay relationship table)

2. **Migration**: Nếu cần, có thể migrate dữ liệu từ array sang relationship table:
   ```sql
   INSERT INTO product_categories (product_id, category)
   SELECT id, unnest(categories) FROM products WHERE categories IS NOT NULL;
   ```

3. **Performance**: 
   - Relationship query thường nhanh hơn với index
   - Native query có thể nhanh hơn nếu array column được index đúng cách

## Kết quả

Sau khi sửa:
- ✅ ProductRepository khởi tạo thành công
- ✅ CartService khởi tạo thành công  
- ✅ CartController khởi tạo thành công
- ✅ Ứng dụng start thành công

## Best Practice

1. **Ưu tiên relationship tables** thay vì array columns cho queries phức tạp
2. **Sử dụng native query** chỉ khi thực sự cần thiết
3. **Document rõ** khi nào dùng method nào
4. **Test cả 2 options** để đảm bảo hoạt động đúng với dữ liệu thực tế

