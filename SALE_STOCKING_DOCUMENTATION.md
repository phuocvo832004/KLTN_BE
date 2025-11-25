# Tài liệu về Sale và Stocking theo Chi nhánh

## Tổng quan

Hệ thống đã được mở rộng với các bảng `branches`, `sales`, và `stockings` để quản lý:
- **Chi nhánh (Branch)**: Quản lý các chi nhánh cửa hàng
- **Sale**: Quản lý chương trình sale theo từng sản phẩm và chi nhánh
- **Stocking**: Quản lý tồn kho theo từng sản phẩm và chi nhánh

## Cấu trúc Database

### 1. Bảng `branches` (Chi nhánh)

```sql
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Các trường:**
- `id`: ID tự động tăng
- `name`: Tên chi nhánh
- `code`: Mã chi nhánh (duy nhất, ví dụ: CN001, CN002)
- `address`: Địa chỉ chi nhánh
- `phone`: Số điện thoại
- `email`: Email
- `is_active`: Trạng thái hoạt động

### 2. Bảng `sales` (Chương trình Sale)

```sql
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    sale_price DECIMAL(38,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, branch_id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE INDEX idx_sale_product_branch ON sales(product_id, branch_id);
CREATE INDEX idx_sale_active ON sales(is_active, start_date, end_date);
```

**Các trường:**
- `id`: ID tự động tăng
- `product_id`: ID sản phẩm
- `branch_id`: ID chi nhánh
- `sale_price`: Giá sale tại chi nhánh này
- `discount_percentage`: Phần trăm giảm giá (0-100)
- `start_date`: Ngày bắt đầu sale
- `end_date`: Ngày kết thúc sale
- `is_active`: Sale có đang hoạt động không

**Ràng buộc:** Mỗi sản phẩm chỉ có một sale tại một chi nhánh (UNIQUE constraint)

### 3. Bảng `stockings` (Tồn kho)

```sql
CREATE TABLE stockings (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    min_stock_level INTEGER NOT NULL DEFAULT 0,
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, branch_id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE INDEX idx_stocking_product_branch ON stockings(product_id, branch_id);
CREATE INDEX idx_stocking_available ON stockings(available_quantity, branch_id);
```

**Các trường:**
- `id`: ID tự động tăng
- `product_id`: ID sản phẩm
- `branch_id`: ID chi nhánh
- `quantity`: Tổng số lượng tồn kho
- `reserved_quantity`: Số lượng đã được đặt trước (chưa xuất kho)
- `available_quantity`: Số lượng có sẵn = quantity - reservedQuantity (tự động tính)
- `min_stock_level`: Mức tồn kho tối thiểu để cảnh báo
- `last_restocked_at`: Lần cuối nhập hàng

**Ràng buộc:** Mỗi sản phẩm chỉ có một bản ghi tồn kho tại một chi nhánh (UNIQUE constraint)

## Use Cases

### Use Case 1: Kiểm tra sản phẩm đang được sale ở chi nhánh nào

```java
// Tìm các chi nhánh đang có sale cho sản phẩm
String productId = "PROD001";
LocalDateTime now = LocalDateTime.now();

List<Branch> branchesWithSale = saleRepository.findBranchesWithActiveSale(productId, now);

// Hoặc lấy thông tin chi tiết sale
List<Sale> activeSales = saleRepository.findActiveSalesByProduct(productId, now);
for (Sale sale : activeSales) {
    System.out.println("Chi nhánh: " + sale.getBranch().getName());
    System.out.println("Giá sale: " + sale.getSalePrice());
    System.out.println("Giảm giá: " + sale.getDiscountPercentage() + "%");
}
```

### Use Case 2: Kiểm tra chi nhánh nào còn hàng

```java
// Tìm các chi nhánh còn hàng cho sản phẩm
String productId = "PROD001";

List<Branch> branchesWithStock = stockingRepository.findBranchesWithStock(productId);

// Hoặc lấy thông tin chi tiết tồn kho
List<Stocking> stockings = stockingRepository.findByProductId(productId);
for (Stocking stocking : stockings) {
    if (stocking.isInStock()) {
        System.out.println("Chi nhánh: " + stocking.getBranch().getName());
        System.out.println("Số lượng có sẵn: " + stocking.getAvailableQuantity());
    }
}
```

### Use Case 3: Kiểm tra sản phẩm vừa có sale vừa còn hàng

```java
String productId = "PROD001";
LocalDateTime now = LocalDateTime.now();

// Lấy các chi nhánh có sale
List<Branch> branchesWithSale = saleRepository.findBranchesWithActiveSale(productId, now);

// Lấy các chi nhánh còn hàng
List<Branch> branchesWithStock = stockingRepository.findBranchesWithStock(productId);

// Tìm giao của 2 danh sách (chi nhánh vừa có sale vừa còn hàng)
List<Branch> branchesWithSaleAndStock = branchesWithSale.stream()
    .filter(branchesWithStock::contains)
    .collect(Collectors.toList());
```

### Use Case 4: Kiểm tra sản phẩm tại một chi nhánh cụ thể

```java
String productId = "PROD001";
Long branchId = 1L;

// Kiểm tra sale
Optional<Sale> sale = saleRepository.findByProductIdAndBranchId(productId, branchId);
if (sale.isPresent() && sale.get().getIsActive()) {
    System.out.println("Đang có sale: " + sale.get().getSalePrice());
}

// Kiểm tra tồn kho
Optional<Stocking> stocking = stockingRepository.findByProductIdAndBranchId(productId, branchId);
if (stocking.isPresent() && stocking.get().isInStock()) {
    System.out.println("Còn hàng: " + stocking.get().getAvailableQuantity());
}
```

## Entities

### Branch Entity
```java
@Entity
@Table(name = "branches")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String code; // Unique
    private String address;
    private String phone;
    private String email;
    private Boolean isActive;
    
    @OneToMany(mappedBy = "branch")
    private List<Sale> sales;
    
    @OneToMany(mappedBy = "branch")
    private List<Stocking> stockings;
}
```

### Sale Entity
```java
@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Product product;
    
    @ManyToOne
    private Branch branch;
    
    private BigDecimal salePrice;
    private BigDecimal discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
}
```

### Stocking Entity
```java
@Entity
@Table(name = "stockings")
public class Stocking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Product product;
    
    @ManyToOne
    private Branch branch;
    
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity; // Auto-calculated
    private Integer minStockLevel;
    private LocalDateTime lastRestockedAt;
    
    // Helper methods
    public Boolean isInStock() {
        return this.availableQuantity > 0;
    }
    
    public Boolean isLowStock() {
        return this.availableQuantity <= this.minStockLevel;
    }
}
```

## Repositories

### BranchRepository
- `findByCode(String code)`: Tìm chi nhánh theo mã
- `findByIsActiveTrue()`: Tìm tất cả chi nhánh đang hoạt động

### SaleRepository
- `findByProductIdAndBranchId(String productId, Long branchId)`: Tìm sale của sản phẩm tại chi nhánh
- `findActiveSalesByProduct(String productId, LocalDateTime now)`: Tìm các sale đang hoạt động của sản phẩm
- `findBranchesWithActiveSale(String productId, LocalDateTime now)`: Tìm các chi nhánh đang có sale cho sản phẩm

### StockingRepository
- `findByProductIdAndBranchId(String productId, Long branchId)`: Tìm tồn kho của sản phẩm tại chi nhánh
- `findBranchesWithStock(String productId)`: Tìm các chi nhánh còn hàng cho sản phẩm
- `findLowStockItemsByBranch(Long branchId)`: Tìm các sản phẩm sắp hết hàng tại chi nhánh
- `findInStockItemsByBranch(Long branchId)`: Tìm các sản phẩm còn hàng tại chi nhánh

## Ví dụ sử dụng trong Service

```java
@Service
public class ProductBranchService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private StockingRepository stockingRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    /**
     * Kiểm tra sản phẩm đang được sale ở chi nhánh nào và chi nhánh nào còn hàng
     */
    public ProductBranchInfo getProductBranchInfo(String productId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy các chi nhánh có sale
        List<Branch> branchesWithSale = saleRepository.findBranchesWithActiveSale(productId, now);
        
        // Lấy các chi nhánh còn hàng
        List<Branch> branchesWithStock = stockingRepository.findBranchesWithStock(productId);
        
        // Lấy thông tin chi tiết
        List<Sale> activeSales = saleRepository.findActiveSalesByProduct(productId, now);
        List<Stocking> stockings = stockingRepository.findByProductId(productId);
        
        return new ProductBranchInfo(
            branchesWithSale,
            branchesWithStock,
            activeSales,
            stockings
        );
    }
}
```

## Migration SQL

Để tạo các bảng trong database, chạy các câu lệnh SQL sau:

```sql
-- Tạo bảng branches
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng sales
CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    sale_price DECIMAL(38,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, branch_id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id)
);

-- Tạo bảng stockings
CREATE TABLE IF NOT EXISTS stockings (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    min_stock_level INTEGER NOT NULL DEFAULT 0,
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, branch_id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (branch_id) REFERENCES branches(id)
);

-- Tạo indexes
CREATE INDEX IF NOT EXISTS idx_sale_product_branch ON sales(product_id, branch_id);
CREATE INDEX IF NOT EXISTS idx_sale_active ON sales(is_active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_stocking_product_branch ON stockings(product_id, branch_id);
CREATE INDEX IF NOT EXISTS idx_stocking_available ON stockings(available_quantity, branch_id);
```

