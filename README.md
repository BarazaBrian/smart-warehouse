# Smart Warehouse and Inventory Management System

A Java desktop application for managing a warehouse's storage-location hierarchy,
products, and stock movements, with a MySQL database behind it.

## Requirements

- Java 26 or higher
- Maven
- MySQL Server, running locally

## Database setup

1. Start MySQL and create an empty database:
   ```sql
   CREATE DATABASE warehouse_db;
   ```
2. In the project root (next to `pom.xml`), create a file called `db.properties`:
   ```
   db.url=jdbc:mysql://localhost:3306/warehouse_db
   db.user=root
   db.password=YOUR_PASSWORD_HERE
   ```
   This file is not committed to the repository (see `.gitignore`), each machine
   running the app needs its own copy with its own local MySQL credentials.
3. No manual table creation is needed. The application creates all four tables
   (`storage_location`, `products`, `inventory_items`, `stock_movements`)
   automatically the first time it runs, in the correct order for their foreign
   keys.

## Building and running from source

```
mvn compile
mvn test
```
Expect 76 passing tests across five test classes, covering recursion, hierarchy rules, product validation, inventory validation, stock movement validation and stock calculations. No database is required for the unit tests.

Then run `Main.java` from your IDE, or:
```
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Running the packaged application

1. Build the standalone jar first: in IntelliJ's Maven panel, run `clean` then `package`. This produces `target\smart-warehouse-1.0-SNAPSHOT.jar` with all dependencies bundled in.
2. From a terminal in the project root, run:
   ```
   & "C:\Program Files\Java\jdk-26.0.1\bin\jpackage.exe" --input target --name SmartWarehouse --main-jar smart-warehouse-1.0-SNAPSHOT.jar --main-class org.example.Main --type app-image --dest dist
   ```
   (Adjust the JDK path if building on a different machine.)
3. This produces `dist\SmartWarehouse\SmartWarehouse.exe` with a bundled Java runtime.
4. Copy `db.properties` into the `dist\SmartWarehouse` folder beside `SmartWarehouse.exe`. Do not commit this file because it contains local database credentials.
5. Ensure MySQL is running and that the `warehouse_db` database exists.
6. Copy the complete `dist\SmartWarehouse` folder to the target Windows computer and double-click `SmartWarehouse.exe`. No separate Java installation is required.
7. Verified on: Windows 11 Pro, version 25H2 (OS build 26200.9445), 15 September 2026.


## Project structure

- `org.example.model` — data classes (`Product`, `StorageLocation`, `InventoryItem`, `StockMovement`)
- `org.example.dao` — all JDBC/SQL, isolated here per the DAO pattern
- `org.example.service` — business logic, validation, and the recursive
  storage-value calculation
- `org.example.ui` — Swing screens (`ProductPanel`, `InventoryPanel`, `StorageLocationPanel`)
- `org.example.util` — database connection and schema setup

## Design patterns

- **DAO pattern**: `ProductDAO`, `StorageLocationDAO`, `InventoryItemDAO`, `StockMovementDAO`
  contain all JDBC and SQL. No SQL appears in the service or UI layers.
- **Composite pattern**: `StorageLocation` represents every level of the hierarchy
  (warehouse, zone, shelf) as one class, so a leaf and a composite are treated
  identically when calculating stock value.

## Recursion

`StorageLocationService.calculateTotalStockValue()` recursively sums stock value
across a storage location and all of its descendants. Base case: a location with
no children returns just its own value. Depth is bounded to three levels
(warehouse/zone/shelf), enforced by `HierarchyRules` when locations are inserted or updated, together
with cycle prevention.


## Team

- Brian Baraza — product management, inventory and stock-movement functions,
  validation, related GUI screens
- Elera Obari Josiah-Chu — storage-location hierarchy, recursive calculation,
  DAO implementation, database schema, related GUI screens