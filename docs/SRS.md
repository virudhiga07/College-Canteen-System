# Software Requirements Specification (SRS)

## 1. Introduction
### 1.1 Purpose
The purpose of the College Canteen Ordering and Sales Analysis System is to manage canteen food items, orders, billing, stock and sales analysis.

### 1.2 Scope
The system supports food-item management, single-item and multi-item orders, automatic bill calculation, stock validation and update, transaction storage, sales reports and Python-based charts.

## 2. Users
- Canteen operator: manages food items and processes orders.
- Project owner/developer: Virudhiga K V (individual academic project owner).

## 3. Functional requirements
1. The system shall store food-item names, prices and stock quantities.
2. The system shall allow adding, updating and deleting food items.
3. The system shall allow an order to contain one or more food items.
4. The system shall calculate the bill automatically.
5. The system shall validate stock before confirming an order.
6. The system shall update stock after a successful order.
7. The system shall store order and order-item details in the database.
8. The system shall support daily and item-wise sales reports.
9. Python shall retrieve and preprocess sales data and generate the required charts.

## 4. Non-functional requirements
- The application shall be implemented using Java 17 and JDBC.
- MySQL shall store application data.
- Maven shall manage the Java project.
- Python, Pandas and Matplotlib shall be used for sales analysis.
- Database updates for an order shall be handled transactionally.

## 5. Data requirements
Food item: item ID, item name, price, available stock.  
Order: order ID, order date/time, total amount.  
Order item: order-item ID, order ID, item ID, quantity, unit price and line total.

## 6. Constraints
The project uses Java 17, Maven, MySQL, JDBC, Python, Pandas and Matplotlib. The project is an individual project owned by Virudhiga K V.
