/* =====================================================================
   MIGRATION: Tuỳ chọn món kiểu menu online (size, đá, đường, topping, ghi chú)
   Ngày: 2026-10-27  -  Người làm: Duong (nhánh DuongCoderSuperPromtAI)

   DÀNH CHO MÁY ĐÃ CÓ DATABASE BOMS CŨ: chạy file này 1 lần trong SSMS.
   - Chạy lại nhiều lần vẫn an toàn (mỗi bước đều kiểm tra đã có hay chưa).
   - KHÔNG xóa dữ liệu: đơn hàng cũ giữ nguyên, các cột mới để NULL.
   Máy cài mới thì chỉ cần chạy BOMS_DATABASE.sql (đã gộp sẵn các thay đổi này).

   Thay đổi:
   1. Products      : thêm Description, VideoUrl, IsFeatured, HasOptions
   2. MenuSizes     : bảng mới - size ly + tiền cộng thêm (Manager quản lý)
   3. MenuToppings  : bảng mới - topping + giá (Manager quản lý)
   4. OrderDetails  : thêm khóa chính OrderDetailID (1 món được nằm nhiều dòng
                      trong cùng 1 đơn nếu khác tuỳ chọn) + các cột lưu bản sao
                      tuỳ chọn tại thời điểm đặt: SizeName, IceLevel, SugarLevel,
                      Toppings, Note
   ===================================================================== */
USE BOMS;
GO

/* ---------- 1. Products ---------- */
IF COL_LENGTH('Products', 'Description') IS NULL
    ALTER TABLE Products ADD Description NVARCHAR(500) NULL;
IF COL_LENGTH('Products', 'VideoUrl') IS NULL
    ALTER TABLE Products ADD VideoUrl VARCHAR(255) NULL;
IF COL_LENGTH('Products', 'IsFeatured') IS NULL
    ALTER TABLE Products ADD IsFeatured BIT NOT NULL CONSTRAINT DF_Products_IsFeatured DEFAULT 0;
IF COL_LENGTH('Products', 'HasOptions') IS NULL
    ALTER TABLE Products ADD HasOptions BIT NOT NULL CONSTRAINT DF_Products_HasOptions DEFAULT 1;
GO

/* ---------- 2. MenuSizes ---------- */
IF OBJECT_ID('MenuSizes', 'U') IS NULL
BEGIN
    CREATE TABLE MenuSizes (
        SizeID      INT IDENTITY(1,1) PRIMARY KEY,
        SizeName    NVARCHAR(20)   NOT NULL UNIQUE,
        ExtraPrice  DECIMAL(12,2)  NOT NULL DEFAULT 0,
        SortOrder   INT            NOT NULL DEFAULT 0,
        IsActive    BIT            NOT NULL DEFAULT 1,
        CONSTRAINT CK_MenuSize_Price CHECK (ExtraPrice >= 0)
    );
END
GO

/* ---------- 3. MenuToppings ---------- */
IF OBJECT_ID('MenuToppings', 'U') IS NULL
BEGIN
    CREATE TABLE MenuToppings (
        ToppingID    INT IDENTITY(1,1) PRIMARY KEY,
        ToppingName  NVARCHAR(50)   NOT NULL UNIQUE,
        Price        DECIMAL(12,2)  NOT NULL DEFAULT 0,
        IsActive     BIT            NOT NULL DEFAULT 1,
        CONSTRAINT CK_MenuTopping_Price CHECK (Price >= 0)
    );
END
GO

/* ---------- 4. OrderDetails ---------- */
-- 4a. Đổi khóa chính (OrderID, ProductID) -> OrderDetailID.
--     Tên khóa chính cũ do SQL Server tự sinh, mỗi máy một tên khác nhau nên phải tra cứu.
IF COL_LENGTH('OrderDetails', 'OrderDetailID') IS NULL
BEGIN
    DECLARE @oldPk SYSNAME = (SELECT name FROM sys.key_constraints
                              WHERE type = 'PK' AND parent_object_id = OBJECT_ID('OrderDetails'));
    IF @oldPk IS NOT NULL
        EXEC('ALTER TABLE OrderDetails DROP CONSTRAINT [' + @oldPk + ']');
    EXEC('ALTER TABLE OrderDetails ADD OrderDetailID INT IDENTITY(1,1) NOT NULL');
    EXEC('ALTER TABLE OrderDetails ADD CONSTRAINT PK_OrderDetails PRIMARY KEY (OrderDetailID)');
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_OrderDetails_OrderID' AND object_id = OBJECT_ID('OrderDetails'))
    CREATE INDEX IX_OrderDetails_OrderID ON OrderDetails(OrderID);
GO

-- 4b. Cột lưu tuỳ chọn (bản sao tại thời điểm đặt, giống UnitPrice)
IF COL_LENGTH('OrderDetails', 'SizeName') IS NULL
    ALTER TABLE OrderDetails ADD SizeName NVARCHAR(20) NULL;
IF COL_LENGTH('OrderDetails', 'IceLevel') IS NULL
    ALTER TABLE OrderDetails ADD IceLevel VARCHAR(20) NULL;      -- mã: ICE_100 / ICE_50 / ICE_0
IF COL_LENGTH('OrderDetails', 'SugarLevel') IS NULL
    ALTER TABLE OrderDetails ADD SugarLevel VARCHAR(20) NULL;    -- mã: SUGAR_100 / SUGAR_70 / SUGAR_0
IF COL_LENGTH('OrderDetails', 'Toppings') IS NULL
    ALTER TABLE OrderDetails ADD Toppings NVARCHAR(255) NULL;    -- tên topping, cách nhau dấu phẩy
IF COL_LENGTH('OrderDetails', 'Note') IS NULL
    ALTER TABLE OrderDetails ADD Note NVARCHAR(200) NULL;
GO

PRINT N'Migration menu options: hoàn tất.';
GO
