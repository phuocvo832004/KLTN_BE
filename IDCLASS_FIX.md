# @IdClass Fix - Sửa lỗi composite key

## Vấn đề

Khi sử dụng `@IdClass` trong JPA, **tên của các field trong entity PHẢI khớp chính xác** với tên của các field trong IdClass.

Lỗi:
```
Entity 'com.fourj.kltn_be.entity.UserPreference' has '@Id' annotated properties 'userId' which do not match properties of the specified '@IdClass'
```

## Nguyên nhân

Trong `UserPreferenceId` class, field được đặt tên là `user` (Long), nhưng trong `UserPreference` entity, field được đặt tên là `userId` (Long).

JPA yêu cầu tên field phải khớp chính xác để có thể map giữa entity và IdClass.

## Giải pháp

### UserPreferenceId - Đã sửa

**Trước:**
```java
public class UserPreferenceId implements Serializable {
    private Long user;  // ❌ Không khớp với userId trong entity
    private String prefKey;
}
```

**Sau:**
```java
public class UserPreferenceId implements Serializable {
    private Long userId;  // ✅ Khớp với userId trong entity
    private String prefKey;
}
```

Cũng cần cập nhật `equals()` và `hashCode()` methods để sử dụng `userId` thay vì `user`.

### ProductSpecId - OK

`ProductSpecId` đã đúng vì:
- `ProductSpecId`: `productId` và `specKey`
- `ProductSpec`: `productId` và `specKey`

Cả hai đều khớp chính xác.

## Quy tắc khi sử dụng @IdClass

1. **Tên field phải khớp chính xác**: Tên field trong IdClass phải giống hệt tên field trong entity (có `@Id` annotation)

2. **Kiểu dữ liệu phải khớp**: Kiểu dữ liệu của các field cũng phải khớp

3. **Thứ tự không quan trọng**: Thứ tự của các field trong IdClass không cần phải giống với thứ tự trong entity

4. **equals() và hashCode()**: Phải implement đúng với tên field mới

## Ví dụ đúng

```java
// Entity
@Entity
@IdClass(MyIdClass.class)
public class MyEntity {
    @Id
    private Long userId;  // Tên: userId
    
    @Id
    private String key;   // Tên: key
}

// IdClass
public class MyIdClass implements Serializable {
    private Long userId;  // ✅ Khớp với userId trong entity
    private String key;   // ✅ Khớp với key trong entity
}
```

## Ví dụ sai

```java
// Entity
@Entity
@IdClass(MyIdClass.class)
public class MyEntity {
    @Id
    private Long userId;  // Tên: userId
}

// IdClass
public class MyIdClass implements Serializable {
    private Long user;  // ❌ Không khớp! Phải là userId
}
```

## Kết quả

Sau khi sửa, backend sẽ khởi động thành công mà không có lỗi `@IdClass` mismatch.

