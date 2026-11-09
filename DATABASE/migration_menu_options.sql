USE BOMS;
GO

IF COL_LENGTH('Products', 'Description') IS NULL
    ALTER TABLE Products ADD Description NVARCHAR(500) NULL;
IF COL_LENGTH('Products', 'VideoUrl') IS NULL
    ALTER TABLE Products ADD VideoUrl VARCHAR(255) NULL;
IF COL_LENGTH('Products', 'IsFeatured') IS NULL
    ALTER TABLE Products ADD IsFeatured BIT NOT NULL CONSTRAINT DF_Products_IsFeatured DEFAULT 0;
IF COL_LENGTH('Products', 'HasOptions') IS NULL
    ALTER TABLE Products ADD HasOptions BIT NOT NULL CONSTRAINT DF_Products_HasOptions DEFAULT 1;
GO

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

IF COL_LENGTH('OrderDetails', 'SizeName') IS NULL
    ALTER TABLE OrderDetails ADD SizeName NVARCHAR(20) NULL;
IF COL_LENGTH('OrderDetails', 'IceLevel') IS NULL
    ALTER TABLE OrderDetails ADD IceLevel VARCHAR(20) NULL;
IF COL_LENGTH('OrderDetails', 'SugarLevel') IS NULL
    ALTER TABLE OrderDetails ADD SugarLevel VARCHAR(20) NULL;
IF COL_LENGTH('OrderDetails', 'Toppings') IS NULL
    ALTER TABLE OrderDetails ADD Toppings NVARCHAR(255) NULL;
IF COL_LENGTH('OrderDetails', 'Note') IS NULL
    ALTER TABLE OrderDetails ADD Note NVARCHAR(200) NULL;
GO

PRINT N'Migration menu options: hoàn tất.';
GO
