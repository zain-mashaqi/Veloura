# VelouraFX - Enterprise Store Management System

## Overview

VelouraFX is a professional enterprise-level Store Management System developed using JavaFX and MySQL.
The system simulates a real-world retail management environment for fashion, cosmetics, and accessories stores.

The project focuses on:

* Inventory management
* Sales and invoice processing
* Product variants management
* Returns workflow
* User roles and authorization
* Revenue analytics and statistics
* Secure authentication
* Professional animated UI/UX

The system was built using Java, JavaFX, JDBC, MySQL, CSS, and Maven following a modular MVC architecture.

---

# Technologies Used

## Backend

* Java
* JDBC
* MySQL

## Frontend

* JavaFX
* FXML
* CSS

## Tools

* Apache NetBeans
* Scene Builder
* DBeaver
* Maven

---

# Project Architecture

The project follows the MVC architecture:

```text id="ztpc1s"
src/
│
├── controller/
├── database/
├── security/
├── util/
├── model/
├── resources/
│   ├── fxml/
│   ├── css/
│   └── images/
```

---

# Main Features

## Authentication System

* Secure login system
* SHA-256 password hashing
* User session management
* Password verification
* Role-based access control

### Security Classes

* `PasswordUtil.java`
* `AuthorizationService.java`
* `UserSession.java`

---

# User Roles

The system supports three different roles:

| Role    | Permissions                            |
| ------- | -------------------------------------- |
| ADMIN   | Full system access                     |
| MANAGER | Products, inventory, sales, statistics |
| CASHIER | Sales and customer operations          |

The authorization system dynamically controls:

* Screen access
* Button visibility
* System operations
* Admin-only actions

---

# Dashboard Module

The dashboard provides:

* Revenue overview
* Product statistics
* Navigation system
* Analytics cards
* Dynamic charts
* Animated UI components

### Files

* `Dashboard.fxml`
* `DashboardController.java`

---

# Product Management

The product system supports:

* Add products
* Edit products
* Delete products
* Brand management
* Category management
* Product variants
* Barcode support
* Size and color variations

### Files

* `Product.fxml`
* `ProductController.java`

---

# Inventory Management

The inventory module includes:

* Real-time stock tracking
* Low stock alerts
* Out-of-stock detection
* Expiry tracking
* Cosmetics monitoring
* Stock additions
* Quantity updates

### Files

* `Inventory.fxml`
* `AddStock.fxml`
* `InventoryController.java`
* `AddStockController.java`

---

# Customer Management

The customer module supports:

* Customer registration
* Customer editing
* Customer search
* Loyalty points system
* Birthday coupons
* Customer history

### Files

* `Customers.fxml`
* `AddCustomer.fxml`
* `CustomersController.java`

---

# Sales Management

The sales system includes:

* Add new sales
* Customer selection
* Discounts system
* Birthday coupon support
* Multiple payment methods
* Automatic invoice generation
* Search and filtering
* Revenue calculations

### Files

* `Sales.fxml`
* `AddSale.fxml`
* `Invoice.fxml`
* `SalesController.java`
* `AddSaleController.java`
* `InvoiceController.java`

---

# Returns Management

The returns workflow includes:

* Invoice-based returns
* Quantity validation
* Return reasons
* Stock restoration
* Return history
* Stock movement registration

### Files

* `Returns.fxml`
* `ReturnsController.java`

---

# Statistics & Analytics

The statistics system provides:

* Pie charts
* Revenue line charts
* Best-selling products
* Revenue tracking
* Total sold products
* Dynamic chart styling

### Files

* `ProductStats.fxml`
* `ProductStatsController.java`
* `product-stats.css`

---

# Users Management

The users module includes:

* Add users
* Change roles
* Activate/deactivate users
* Password reset requests
* Admin password management

### Files

* `Users.fxml`
* `UsersController.java`

---

# Welcome & UI Experience

The project includes:

* Animated welcome screen
* Smooth transitions
* Hover effects
* Toast notifications
* Luxury dark theme
* Gold accent styling

### Files

* `Welcome.fxml`
* `LogoutWelcome.fxml`
* `WelcomeController.java`
* `ToastNotification.java`

---

# Database Design

## Database Name

```sql id="0d7a44"
veloura_db
```

---

# Main Database Tables

## Users & Security

* users

## Customers

* customers
* loyalty_transactions

## Products & Inventory

* products
* product_variants
* stock_movements

## Sales System

* sales
* sale_items
* payments

## Returns System

* returns
* return_items

## Purchases & Suppliers

* suppliers
* purchases
* purchase_items

---

# Database Features

The database includes:

* Foreign key relationships
* Relational structure
* Cascading operations
* Product variants support
* Sales tracking
* Inventory synchronization
* Return validation
* Loyalty tracking
* Expiry monitoring

---

# Database Script

The project contains a complete MySQL database script:

```text id="r8s5w4"
veloura_db1.sql
```

The script includes:

* Table creation
* Relationships
* Constraints
* Enterprise test data
* Inventory data
* Sales records
* Returns data
* User accounts
* Loyalty system data

---

# Inventory Workflow

## Purchase Process

1. Supplier provides products
2. Purchase records are created
3. Stock quantities increase
4. Stock movements are registered

## Sales Process

1. Customer selects products
2. Sale invoice is generated
3. Discounts are applied
4. Inventory updates automatically

## Returns Process

1. Existing invoice is selected
2. Return quantity validated
3. Stock restored automatically
4. Return history saved

---

# Security Features

The system implements:

* SHA-256 hashing
* Session tracking
* Permission validation
* Secure password verification
* Role restrictions

---

# UI/UX Features

The UI design includes:

* Luxury dark navy theme
* Gold accent colors
* Responsive layouts
* Animated transitions
* Interactive buttons
* Professional dashboard cards
* Dynamic charts

---

# Main Application Entry

```text id="t0sg7u"
App.java
```

The application starts from:

* `Login.fxml`

and dynamically navigates between all screens.

---

# How to Run the Project

## 1. Clone Repository

```bash id="kj6gmd"
git clone <repository-link>
```

---

## 2. Open Project

Open the project using:

* Apache NetBeans
* IntelliJ IDEA

---

## 3. Create Database

Create a MySQL database named:

```sql id="l2h3fk"
veloura_db
```

---

## 4. Import Database Script

Import:

```text id="zghc56"
veloura_db1.sql
```

using:

* DBeaver
* MySQL Workbench
* phpMyAdmin

---

## 5. Configure Database Connection

Inside:

```text id="9q9zj8"
DBConnection.java
```

update:

```java id="x9cqgl"
private static final String URL =
    "jdbc:mysql://127.0.0.1:3306/veloura_db?useSSL=false&serverTimezone=UTC";

private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

---

## 6. Run Application

Run:

```text id="4zjz2r"
App.java
```

The application will launch starting from the Login screen.

---

# Enterprise Features

VelouraFX includes enterprise-level features such as:

* Product variants
* Barcode management
* Inventory tracking
* Expiry monitoring
* Revenue analytics
* Role-based authorization
* Sales invoices
* Returns processing
* Loyalty points
* Birthday coupons
* Supplier management
* Purchase tracking
* Stock movement history

---

# Future Improvements

Future enhancements may include:

* Cloud database hosting
* Multi-branch support
* QR code system
* Email notifications
* AI sales prediction
* PDF invoice export
* Excel reporting
* Online order integration

---

# Conclusion

VelouraFX was developed to simulate a realistic enterprise retail management environment using professional software engineering practices.

The project combines:

* Secure authentication
* Real-time inventory management
* Sales and returns workflow
* Revenue analytics
* Advanced UI/UX
* Enterprise database architecture

to create a complete modern Store Management System.
