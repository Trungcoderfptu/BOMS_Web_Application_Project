/* =====================================================================
   BINGXUE OPERATIONS MANAGEMENT SYSTEM (BOMS) - DATABASE
   DBMS: SQL Server
   ---------------------------------------------------------------------
   QUAN HE CHINH
   1 - 1 : Users            - ShipperProfiles   (moi shipper co 1 ho so)
           Orders           - Payments          (moi don co 1 thanh toan)
           ShiftAssignments - Attendances       (moi ca phan cong co 1 ban ghi cham cong)
   1 - n : Users            - Orders (nhan vien tao don)
           MenuCategories   - Products
           IngredientCategories - Ingredients
           Ingredients      - IngredientBatches (lo nhap, han su dung)
           Ingredients      - InventoryAlerts
           DiningTables     - Orders
           DeliveryAreas    - Orders
           Orders           - DeliveryStatusHistory
           ReportCategories - AnonymousReports
           AnonymousReports - ReportEvidences
           Users            - Notifications
   n - n : Orders     <-> Products     qua OrderDetails
           Products   <-> Ingredients  qua ProductIngredients (cong thuc)
           Users      <-> Shifts       qua ShiftAssignments
           Shippers   <-> DeliveryAreas qua ShipperAreas
           Managers   <-> ReportCategories qua CategoryManagers
   Da tri : Users -> UserPhones (1 nguoi nhieu so dien thoai phu)
   ===================================================================== */

IF DB_ID('BOMS') IS NULL
    CREATE DATABASE BOMS;
GO
USE BOMS;
GO

/* =====================================================================
   1. NGUOI DUNG
   ===================================================================== */
CREATE TABLE Users (
    UserID        INT IDENTITY(1,1) PRIMARY KEY,
    Username      VARCHAR(50)    NOT NULL UNIQUE,
    Password      VARCHAR(255)   NOT NULL,
    FullName      NVARCHAR(100)  NOT NULL,
    PrimaryEmail  VARCHAR(100)   NULL UNIQUE,
    PrimaryPhone  VARCHAR(20)    NULL,
    Role          VARCHAR(20)    NOT NULL,
    HireDate      DATE           NULL,
    Address       NVARCHAR(255)  NULL,
    IsActive      BIT            NOT NULL DEFAULT 1,
    CONSTRAINT CK_Users_Role CHECK (Role IN ('Admin', 'Manager', 'Staff', 'Shipper'))
);

-- Thuoc tinh da tri: 1 user co nhieu so dien thoai phu
CREATE TABLE UserPhones (
    UserID     INT          NOT NULL,
    Phone      VARCHAR(20)  NOT NULL,
    PRIMARY KEY (UserID, Phone),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

/* =====================================================================
   2. GIAO HANG - UC-09, UC-10
   ===================================================================== */
CREATE TABLE DeliveryAreas (
    AreaID     INT IDENTITY(1,1) PRIMARY KEY,
    AreaName   NVARCHAR(100)  NOT NULL UNIQUE
);

-- Quan he 1-1 voi Users: PK cung la FK
CREATE TABLE ShipperProfiles (
    UserID          INT          PRIMARY KEY,
    VehiclePlate    VARCHAR(20)  NULL,
    ShipperStatus   VARCHAR(20)  NOT NULL DEFAULT 'Offline',
    ActiveOrders    INT          NOT NULL DEFAULT 0,
    MaxOrders       INT          NOT NULL DEFAULT 3,      -- gioi han gom don (9.1)
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT CK_Shipper_Status CHECK (ShipperStatus IN ('Offline', 'Ready', 'Delivering'))
);

-- n-n: shipper phu trach nhieu khu vuc, khu vuc co nhieu shipper
CREATE TABLE ShipperAreas (
    ShipperID  INT NOT NULL,
    AreaID     INT NOT NULL,
    PRIMARY KEY (ShipperID, AreaID),
    FOREIGN KEY (ShipperID) REFERENCES ShipperProfiles(UserID),
    FOREIGN KEY (AreaID)    REFERENCES DeliveryAreas(AreaID)
);

/* =====================================================================
   3. KHO NGUYEN LIEU - UC-03, UC-04, Add/Delete Ingredient,
      Set Warning Threshold, Expired Ingredient Alert
   ===================================================================== */
CREATE TABLE IngredientCategories (
    CategoryID    INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName  NVARCHAR(100)  NOT NULL UNIQUE
);

CREATE TABLE Ingredients (
    IngredientID      INT IDENTITY(1,1) PRIMARY KEY,
    CategoryID        INT            NOT NULL,
    IngredientName    NVARCHAR(100)  NOT NULL UNIQUE,
    Unit              NVARCHAR(20)   NOT NULL,            -- kg, lit, hop...
    Quantity          DECIMAL(10,2)  NOT NULL DEFAULT 0,  -- tong ton kho
    Price             DECIMAL(12,2)  NOT NULL DEFAULT 0,
    StorageLocation   NVARCHAR(100)  NULL,
    WarningThreshold  DECIMAL(10,2)  NULL,                -- nguong canh bao do manager dat
    IsActive          BIT            NOT NULL DEFAULT 1,  -- xoa mem
    FOREIGN KEY (CategoryID) REFERENCES IngredientCategories(CategoryID),
    CONSTRAINT CK_Ingredient_Qty CHECK (Quantity >= 0)
);

-- 1 nguyen lieu co nhieu lo nhap, moi lo co han su dung rieng
CREATE TABLE IngredientBatches (
    BatchID         INT IDENTITY(1,1) PRIMARY KEY,
    IngredientID    INT            NOT NULL,
    Quantity        DECIMAL(10,2)  NOT NULL,
    DateReceived    DATE           NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    ExpirationDate  DATE           NOT NULL,
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID),
    CONSTRAINT CK_Batch_Date CHECK (ExpirationDate >= DateReceived)
);

CREATE TABLE InventoryAlerts (
    AlertID        INT IDENTITY(1,1) PRIMARY KEY,
    IngredientID   INT          NOT NULL,
    AlertType      VARCHAR(20)  NOT NULL,                -- LowStock, OutOfStock, Expired
    Status         VARCHAR(20)  NOT NULL DEFAULT 'Active',
    ConfirmedBy    INT          NULL,
    CreatedAt      DATETIME     NOT NULL DEFAULT GETDATE(),
    ConfirmedAt    DATETIME     NULL,
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID),
    FOREIGN KEY (ConfirmedBy)  REFERENCES Users(UserID),
    CONSTRAINT CK_Alert_Type   CHECK (AlertType IN ('LowStock', 'OutOfStock', 'Expired')),
    CONSTRAINT CK_Alert_Status CHECK (Status IN ('Active', 'Confirmed', 'Closed'))
);

/* =====================================================================
   4. MENU
   ===================================================================== */
CREATE TABLE MenuCategories (
    CategoryID    INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName  NVARCHAR(100)  NOT NULL UNIQUE
);

CREATE TABLE Products (
    ProductID     INT IDENTITY(1,1) PRIMARY KEY,
    CategoryID    INT            NOT NULL,
    ProductName   NVARCHAR(100)  NOT NULL,
    Price         DECIMAL(12,2)  NOT NULL,
    ImageUrl      VARCHAR(255)   NULL,
    IsAvailable   BIT            NOT NULL DEFAULT 1,     -- het hang -> 0 (1.0.E1, 2.0.E1)
    FOREIGN KEY (CategoryID) REFERENCES MenuCategories(CategoryID),
    CONSTRAINT CK_Product_Price CHECK (Price >= 0)
);

-- n-n: cong thuc - 1 mon dung nhieu nguyen lieu, 1 nguyen lieu dung cho nhieu mon
-- dung de tu dong tru kho khi tao don (UC-01, UC-02 POST-3)
CREATE TABLE ProductIngredients (
    ProductID     INT            NOT NULL,
    IngredientID  INT            NOT NULL,
    QuantityUsed  DECIMAL(10,2)  NOT NULL,
    PRIMARY KEY (ProductID, IngredientID),
    FOREIGN KEY (ProductID)    REFERENCES Products(ProductID),
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID)
);

/* =====================================================================
   5. DON HANG & THANH TOAN - UC-01, UC-02, Modify/Delete/Change Order,
      Checkout
   ===================================================================== */
CREATE TABLE DiningTables (
    TableID      INT IDENTITY(1,1) PRIMARY KEY,
    TableNumber  VARCHAR(10)   NOT NULL UNIQUE,
    QRCode       VARCHAR(255)  NOT NULL UNIQUE
);

CREATE TABLE Orders (
    OrderID          INT IDENTITY(1,1) PRIMARY KEY,
    OrderType        VARCHAR(20)    NOT NULL,            -- Counter, Table, Delivery
    StaffID          INT            NULL,                -- nhan vien tao don tai quay
    TableID          INT            NULL,                -- don tai ban qua QR
    AreaID           INT            NULL,                -- don giao hang
    ShipperID        INT            NULL,
    CustomerName     NVARCHAR(100)  NULL,
    CustomerPhone    VARCHAR(20)    NULL,
    DeliveryAddress  NVARCHAR(255)  NULL,
    Status           VARCHAR(30)    NOT NULL DEFAULT 'Accepted',
    TotalAmount      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    CreatedAt        DATETIME       NOT NULL DEFAULT GETDATE(),
    CookingStartedAt DATETIME       NULL,                -- chi duoc doi mon khi NULL (2.1)
    FOREIGN KEY (StaffID)   REFERENCES Users(UserID),
    FOREIGN KEY (TableID)   REFERENCES DiningTables(TableID),
    FOREIGN KEY (AreaID)    REFERENCES DeliveryAreas(AreaID),
    FOREIGN KEY (ShipperID) REFERENCES ShipperProfiles(UserID),
    CONSTRAINT CK_Order_Type CHECK (OrderType IN ('Counter', 'Table', 'Delivery')),
    CONSTRAINT CK_Order_Status CHECK (Status IN (
        'Accepted', 'Paid', 'Prepared', 'Waiting for dispatch', 'Waiting for delivery',
        'Picked up', 'Delivering', 'Delivered', 'Returned', 'Cancelled'))
);

-- n-n: Orders <-> Products
CREATE TABLE OrderDetails (
    OrderID     INT            NOT NULL,
    ProductID   INT            NOT NULL,
    Quantity    INT            NOT NULL,
    UnitPrice   DECIMAL(12,2)  NOT NULL,                 -- gia tai thoi diem ban
    PRIMARY KEY (OrderID, ProductID),
    FOREIGN KEY (OrderID)   REFERENCES Orders(OrderID) ON DELETE CASCADE,
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT CK_Detail_Qty CHECK (Quantity > 0)
);

-- 1-1 voi Orders: OrderID UNIQUE
CREATE TABLE Payments (
    PaymentID        INT IDENTITY(1,1) PRIMARY KEY,
    OrderID          INT            NOT NULL UNIQUE,
    Method           VARCHAR(20)    NOT NULL,            -- Cash, Online
    Amount           DECIMAL(12,2)  NOT NULL,
    TransactionCode  VARCHAR(100)   NULL,                -- ma giao dich cong thanh toan
    Status           VARCHAR(20)    NOT NULL DEFAULT 'Success',
    PaidAt           DATETIME       NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT CK_Pay_Method CHECK (Method IN ('Cash', 'Online')),
    CONSTRAINT CK_Pay_Status CHECK (Status IN ('Success', 'Failed'))
);

-- UC-10: luu moi lan shipper cap nhat trang thai (bao cao toc do giao hang)
CREATE TABLE DeliveryStatusHistory (
    HistoryID    INT IDENTITY(1,1) PRIMARY KEY,
    OrderID      INT            NOT NULL,
    ShipperID    INT            NOT NULL,
    Status       VARCHAR(30)    NOT NULL,
    FailReason   NVARCHAR(255)  NULL,                    -- 10.0.E1
    ProofImage   VARCHAR(255)   NULL,
    UpdatedAt    DATETIME       NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (OrderID)   REFERENCES Orders(OrderID),
    FOREIGN KEY (ShipperID) REFERENCES ShipperProfiles(UserID)
);

/* =====================================================================
   6. CHAM CONG - UC-05, UC-06, View Timesheet Summary
   ===================================================================== */
CREATE TABLE Shifts (
    ShiftID     INT IDENTITY(1,1) PRIMARY KEY,
    ShiftName   NVARCHAR(50)  NOT NULL,
    StartTime   TIME          NOT NULL,
    EndTime     TIME          NOT NULL
);

-- n-n: Users <-> Shifts theo ngay
CREATE TABLE ShiftAssignments (
    AssignmentID  INT IDENTITY(1,1) PRIMARY KEY,
    UserID        INT   NOT NULL,
    ShiftID       INT   NOT NULL,
    WorkDate      DATE  NOT NULL,
    FOREIGN KEY (UserID)  REFERENCES Users(UserID),
    FOREIGN KEY (ShiftID) REFERENCES Shifts(ShiftID),
    CONSTRAINT UQ_Assignment UNIQUE (UserID, ShiftID, WorkDate)
);

-- 1-1 voi ShiftAssignments: AssignmentID UNIQUE (chi check-in 1 lan - 5.0.E3)
CREATE TABLE Attendances (
    AttendanceID    INT IDENTITY(1,1) PRIMARY KEY,
    AssignmentID    INT            NOT NULL UNIQUE,
    CheckInTime     DATETIME       NOT NULL,
    CheckInStatus   VARCHAR(30)    NOT NULL,             -- On Time, Late, Pending Early Approval
    CheckOutTime    DATETIME       NULL,
    CheckOutStatus  VARCHAR(30)    NULL,                 -- On Time, Left Early, Pending OT Approval
    WorkingHours    DECIMAL(5,2)   NULL,
    OTReason        NVARCHAR(255)  NULL,                 -- 6.1
    ApprovedBy      INT            NULL,
    FOREIGN KEY (AssignmentID) REFERENCES ShiftAssignments(AssignmentID),
    FOREIGN KEY (ApprovedBy)   REFERENCES Users(UserID),
    CONSTRAINT CK_Att_Time CHECK (CheckOutTime IS NULL OR CheckOutTime > CheckInTime)
);

/* =====================================================================
   7. BAO CAO AN DANH - UC-07, UC-08
   ===================================================================== */
CREATE TABLE ReportCategories (
    CategoryID    INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName  NVARCHAR(50)  NOT NULL UNIQUE           -- HR, Operations...
);

-- n-n: manager nhan bao cao theo loai (UC-07 Other Information)
CREATE TABLE CategoryManagers (
    CategoryID  INT NOT NULL,
    ManagerID   INT NOT NULL,
    PRIMARY KEY (CategoryID, ManagerID),
    FOREIGN KEY (CategoryID) REFERENCES ReportCategories(CategoryID),
    FOREIGN KEY (ManagerID)  REFERENCES Users(UserID)
);

-- KHONG luu UserID nguoi gui -> an danh. TicketID de nguoi gui tra cuu phan hoi
CREATE TABLE AnonymousReports (
    ReportID     INT IDENTITY(1,1) PRIMARY KEY,
    TicketID     VARCHAR(20)    NOT NULL UNIQUE,
    CategoryID   INT            NOT NULL,
    Title        NVARCHAR(200)  NOT NULL,
    Description  NVARCHAR(MAX)  NOT NULL,
    Status       VARCHAR(20)    NOT NULL DEFAULT 'Pending',
    Feedback     NVARCHAR(MAX)  NULL,
    HandledBy    INT            NULL,
    SubmittedAt  DATETIME       NOT NULL DEFAULT GETDATE(),
    UpdatedAt    DATETIME       NULL,
    FOREIGN KEY (CategoryID) REFERENCES ReportCategories(CategoryID),
    FOREIGN KEY (HandledBy)  REFERENCES Users(UserID),
    CONSTRAINT CK_Report_Status CHECK (Status IN ('Pending', 'In Progress', 'Resolved', 'Rejected')),
    -- 8.0.E1: Rejected bat buoc co Feedback
    CONSTRAINT CK_Report_Reject CHECK (Status <> 'Rejected' OR (Feedback IS NOT NULL AND LEN(Feedback) > 0))
);

CREATE TABLE ReportEvidences (
    EvidenceID  INT IDENTITY(1,1) PRIMARY KEY,
    ReportID    INT            NOT NULL,
    FileName    NVARCHAR(255)  NOT NULL,
    FilePath    VARCHAR(500)   NOT NULL,
    FileType    VARCHAR(10)    NOT NULL,
    FileSize    BIGINT         NOT NULL,
    FOREIGN KEY (ReportID) REFERENCES AnonymousReports(ReportID) ON DELETE CASCADE,
    -- 7.0.E1: dinh dang va dung luong toi da 20MB
    CONSTRAINT CK_Evidence_Type CHECK (FileType IN ('jpg', 'jpeg', 'png', 'mp4', 'pdf', 'docx')),
    CONSTRAINT CK_Evidence_Size CHECK (FileSize > 0 AND FileSize <= 20971520)
);

/* =====================================================================
   8. THONG BAO (dung chung: bao cao moi, canh bao kho, don giao...)
   ===================================================================== */
CREATE TABLE Notifications (
    NotificationID  INT IDENTITY(1,1) PRIMARY KEY,
    RecipientID     INT            NOT NULL,
    Type            VARCHAR(30)    NOT NULL,             -- Report, InventoryAlert, Delivery, Attendance
    RefID           INT            NULL,                 -- ID cua doi tuong lien quan
    Message         NVARCHAR(500)  NOT NULL,
    IsRead          BIT            NOT NULL DEFAULT 0,
    CreatedAt       DATETIME       NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (RecipientID) REFERENCES Users(UserID)
);
GO

/* =====================================================================
   9. QUẢN LÝ MÃ BẢO MẬT & TÀI KHOẢN (ACCOUNT MANAGER)
   ===================================================================== */
CREATE TABLE AccountManager (
    KeyID        INT IDENTITY(1,1) PRIMARY KEY,
    SecurityKey  VARCHAR(50) NOT NULL UNIQUE,
    Role         VARCHAR(20) NOT NULL,       
    UserID       INT         NULL,           -- BỎ TỪ KHÓA UNIQUE Ở ĐÂY ĐI
    IsActive     BIT         NOT NULL DEFAULT 1,
    CreatedAt    DATETIME    NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT CK_AccountManager_Role CHECK (Role IN ('Admin', 'Manager', 'Staff', 'Shipper'))
);
GO

-- SỬ DỤNG FILTERED INDEX ĐỂ THAY THẾ UNIQUE
-- (Cho phép vô số giá trị NULL, nhưng các giá trị số UserID phải là duy nhất)
CREATE UNIQUE NONCLUSTERED INDEX UQ_AccountManager_UserID_Smart
ON AccountManager(UserID)
WHERE UserID IS NOT NULL;
GO

--(Trigger): Tự động đồng bộ khóa mã khi User bị đuổi việc/khóa tài khoản
CREATE TRIGGER TRG_Sync_AccountManager_Status
ON Users
AFTER UPDATE
AS
BEGIN
    -- Chỉ kích hoạt khi cột IsActive của bảng Users bị thay đổi
    IF UPDATE(IsActive)
    BEGIN
        UPDATE am
        SET am.IsActive = i.IsActive
        FROM AccountManager am
        JOIN inserted i ON am.UserID = i.UserID;
    END
END;
GO

/* =====================================================================
   DU LIEU MAU
   ===================================================================== */
INSERT INTO Users (Username, Password, FullName, PrimaryEmail, PrimaryPhone, Role, HireDate, Address) VALUES
 ('admin',    '123', N'Quản Trị Hệ Thống',  'admin@bx.com',    '090123', 'Admin',   '2026-01-01', N'Hà Nội'),
 ('manager1', '123', N'Trần Văn Quản Lý',   'manager@bx.com',  '090789', 'Manager', '2026-02-01', N'Hà Nội'),
 ('staff1',   '123', N'Nhân Viên Chạy Bàn', 'staff@bx.com',    '090456', 'Staff',   '2026-06-15', N'Hòa Bình'),
 ('shipper1', '123', N'Lê Văn Giao',        'shipper@bx.com',  '090999', 'Shipper', '2026-05-10', N'Hà Nội');

INSERT INTO UserPhones VALUES (3, '0911111111'), (3, '0922222222');

INSERT INTO DeliveryAreas (AreaName) VALUES (N'Cầu Giấy'), (N'Đống Đa');
INSERT INTO ShipperProfiles (UserID, VehiclePlate, ShipperStatus) VALUES (4, '29A1-12345', 'Ready');
INSERT INTO ShipperAreas VALUES (4, 1), (4, 2);

INSERT INTO IngredientCategories (CategoryName) VALUES (N'Sữa'), (N'Trái cây'), (N'Đường');
INSERT INTO Ingredients (CategoryID, IngredientName, Unit, Quantity, Price, StorageLocation, WarningThreshold) VALUES
 (1, N'Sữa tươi',   N'lít', 20, 30000, N'Tủ lạnh 1', 5),
 (2, N'Dâu tây',    N'kg',  3,  80000, N'Tủ lạnh 2', 5),
 (3, N'Đường trắng', N'kg', 10, 20000, N'Kệ A',      2);
INSERT INTO IngredientBatches (IngredientID, Quantity, DateReceived, ExpirationDate) VALUES
 (1, 20, '2026-09-28', '2026-10-05'),
 (2, 3,  '2026-09-30', '2026-10-03');
INSERT INTO InventoryAlerts (IngredientID, AlertType) VALUES (2, 'LowStock');

INSERT INTO MenuCategories (CategoryName) VALUES (N'Kem'), (N'Trà sữa');
INSERT INTO Products (CategoryID, ProductName, Price) VALUES
 (1, N'Kem dâu', 25000), (2, N'Trà sữa trân châu', 30000);
INSERT INTO ProductIngredients VALUES (1, 1, 0.1), (1, 2, 0.05), (2, 1, 0.2), (2, 3, 0.03);

INSERT INTO DiningTables (TableNumber, QRCode) VALUES ('T01', 'QR-T01'), ('T02', 'QR-T02');

INSERT INTO Orders (OrderType, StaffID, Status, TotalAmount) VALUES ('Counter', 3, 'Accepted', 55000);
INSERT INTO Orders (OrderType, TableID, Status, TotalAmount) VALUES ('Table', 1, 'Paid', 30000);
INSERT INTO Orders (OrderType, AreaID, ShipperID, CustomerName, CustomerPhone, DeliveryAddress, Status, TotalAmount)
 VALUES ('Delivery', 1, 4, N'Nguyễn Khách', '0988888888', N'12 Xuân Thủy', 'Delivering', 50000);
INSERT INTO OrderDetails VALUES (1, 1, 1, 25000), (1, 2, 1, 30000), (2, 2, 1, 30000), (3, 1, 2, 25000);
INSERT INTO Payments (OrderID, Method, Amount) VALUES (1, 'Cash', 55000);
INSERT INTO Payments (OrderID, Method, Amount, TransactionCode) VALUES (2, 'Online', 30000, 'VNP123456');
INSERT INTO DeliveryStatusHistory (OrderID, ShipperID, Status) VALUES (3, 4, 'Picked up'), (3, 4, 'Delivering');

INSERT INTO Shifts (ShiftName, StartTime, EndTime) VALUES (N'Ca sáng', '07:00', '15:00'), (N'Ca chiều', '15:00', '23:00');
INSERT INTO ShiftAssignments (UserID, ShiftID, WorkDate) VALUES (3, 1, '2026-10-01'), (3, 1, '2026-10-02');
INSERT INTO Attendances (AssignmentID, CheckInTime, CheckInStatus, CheckOutTime, CheckOutStatus, WorkingHours) VALUES
 (1, '2026-10-01 06:58', 'On Time', '2026-10-01 15:05', 'On Time', 8.1);

INSERT INTO ReportCategories (CategoryName) VALUES (N'HR'), (N'Operations');
INSERT INTO CategoryManagers VALUES (1, 2), (2, 2);
INSERT INTO AnonymousReports (TicketID, CategoryID, Title, Description) VALUES
 ('TK-0001', 2, N'Không rửa tay khi pha chế', N'Nhân viên ca tối không rửa tay trước khi pha chế.');
INSERT INTO ReportEvidences (ReportID, FileName, FilePath, FileType, FileSize) VALUES
 (1, N'anh1.jpg', '/uploads/reports/1/anh1.jpg', 'jpg', 204800);
INSERT INTO Notifications (RecipientID, Type, RefID, Message) VALUES
 (2, 'Report', 1, N'Có báo cáo ẩn danh mới: TK-0001'),
 (2, 'InventoryAlert', 1, N'Dâu tây sắp hết hàng');
GO

/* =====================================================================
   CAU LENH SELECT MAU
   ===================================================================== */

-- Xem tung bang
SELECT * FROM Users;
SELECT * FROM Orders;
SELECT * FROM Ingredients;
SELECT * FROM AnonymousReports;

-- [Da tri] User kem cac so dien thoai phu
SELECT u.UserID, u.FullName, u.PrimaryPhone, p.Phone AS ExtraPhone
FROM Users u LEFT JOIN UserPhones p ON u.UserID = p.UserID;

-- [1-1] Thong tin shipper
SELECT u.FullName, s.VehiclePlate, s.ShipperStatus, s.ActiveOrders
FROM Users u JOIN ShipperProfiles s ON u.UserID = s.UserID;

-- [n-n] Chi tiet don hang (UC-01, UC-02)
SELECT o.OrderID, o.OrderType, p.ProductName, d.Quantity, d.UnitPrice,
       d.Quantity * d.UnitPrice AS LineTotal
FROM Orders o
JOIN OrderDetails d ON o.OrderID = d.OrderID
JOIN Products p     ON d.ProductID = p.ProductID;

-- [1-1] Don hang kem thanh toan
SELECT o.OrderID, o.TotalAmount, pay.Method, pay.TransactionCode, pay.PaidAt
FROM Orders o LEFT JOIN Payments pay ON o.OrderID = pay.OrderID;

-- [n-n] Cong thuc mon an
SELECT p.ProductName, i.IngredientName, pi.QuantityUsed, i.Unit
FROM Products p
JOIN ProductIngredients pi ON p.ProductID = pi.ProductID
JOIN Ingredients i         ON pi.IngredientID = i.IngredientID;

-- UC-03: tim nguyen lieu theo ten hoac loai
SELECT i.IngredientID, i.IngredientName, c.CategoryName, i.Quantity, i.Unit,
       i.Price, i.StorageLocation, b.DateReceived, b.ExpirationDate
FROM Ingredients i
JOIN IngredientCategories c   ON i.CategoryID = c.CategoryID
LEFT JOIN IngredientBatches b ON i.IngredientID = b.IngredientID
WHERE i.IsActive = 1 AND (i.IngredientName LIKE N'%dâu%' OR c.CategoryName LIKE N'%dâu%');

-- UC-04: nguyen lieu cham nguong canh bao
SELECT IngredientName, Quantity, WarningThreshold
FROM Ingredients
WHERE IsActive = 1 AND Quantity <= WarningThreshold;

-- Expired Ingredient Alert: lo sap het han trong 3 ngay
SELECT i.IngredientName, b.Quantity, b.ExpirationDate
FROM IngredientBatches b JOIN Ingredients i ON b.IngredientID = i.IngredientID
WHERE b.ExpirationDate <= DATEADD(DAY, 3, CAST(GETDATE() AS DATE));

-- UC-09: chon shipper phu hop nhat trong khu vuc (it don nhat)
SELECT TOP 1 s.UserID, u.FullName, s.ActiveOrders
FROM ShipperProfiles s
JOIN ShipperAreas sa ON s.UserID = sa.ShipperID
JOIN Users u         ON s.UserID = u.UserID
WHERE sa.AreaID = 1 AND s.ShipperStatus = 'Ready' AND s.ActiveOrders < s.MaxOrders
ORDER BY s.ActiveOrders;

-- UC-10: lich su trang thai giao hang
SELECT h.OrderID, u.FullName AS Shipper, h.Status, h.UpdatedAt
FROM DeliveryStatusHistory h JOIN Users u ON h.ShipperID = u.UserID
ORDER BY h.OrderID, h.UpdatedAt;

-- UC-05/06 + View Timesheet Summary: tong gio lam theo thang
SELECT u.FullName, COUNT(a.AttendanceID) AS TotalShifts, SUM(a.WorkingHours) AS TotalHours
FROM Users u
JOIN ShiftAssignments sa ON u.UserID = sa.UserID
JOIN Attendances a       ON sa.AssignmentID = a.AssignmentID
WHERE MONTH(sa.WorkDate) = 10 AND YEAR(sa.WorkDate) = 2026
GROUP BY u.FullName;

-- UC-08: danh sach bao cao Pending kem so file bang chung (loc theo Category)
SELECT r.TicketID, c.CategoryName, r.Title, r.Status, r.SubmittedAt,
       COUNT(e.EvidenceID) AS EvidenceCount
FROM AnonymousReports r
JOIN ReportCategories c     ON r.CategoryID = c.CategoryID
LEFT JOIN ReportEvidences e ON r.ReportID = e.ReportID
WHERE r.Status = 'Pending'
GROUP BY r.TicketID, c.CategoryName, r.Title, r.Status, r.SubmittedAt
ORDER BY r.SubmittedAt DESC;

-- [n-n] Manager nhan bao cao theo loai
SELECT c.CategoryName, u.FullName AS Manager
FROM CategoryManagers cm
JOIN ReportCategories c ON cm.CategoryID = c.CategoryID
JOIN Users u            ON cm.ManagerID = u.UserID;

-- Thong bao chua doc cua 1 user
SELECT * FROM Notifications WHERE RecipientID = 2 AND IsRead = 0 ORDER BY CreatedAt DESC;
