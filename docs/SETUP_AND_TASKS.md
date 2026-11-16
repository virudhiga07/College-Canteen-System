# Complete setup and submission checklist

## A. Software setup
- [ ] JDK 17 installed and `java -version` works.
- [ ] `JAVA_HOME` points to `C:\\Program Files\\Java\\jdk-17`.
- [ ] Maven extracted to `D:\\apache-maven-3.10.0`.
- [ ] `mvn -version` works.
- [ ] MySQL Server installed and running.
- [ ] MySQL Workbench installed and can connect to the local server.
- [ ] Python 3 installed and `python --version` works.
- [ ] VS Code opens `D:\\College-Canteen-System`.

## B. Database
- [ ] Run `database/schema.sql` in MySQL Workbench.
- [ ] Run `database/sample_data.sql`.
- [ ] Check that `food_items`, `orders` and `order_items` tables exist.
- [ ] Test the SQL queries in `database/queries.sql`.
- [ ] Document primary keys, foreign keys, relationships and normalization.

## C. Java application
Implement and test these features:
- [ ] Display food menu with price and available stock.
- [ ] Add a food item.
- [ ] Update a food item's name, price or stock.
- [ ] Delete a food item.
- [ ] Create an order with multiple food items and quantities.
- [ ] Reject an order when requested quantity exceeds available stock.
- [ ] Calculate item totals and grand total automatically.
- [ ] Save order and order details.
- [ ] Update stock only when the order is successfully saved.
- [ ] Use a JDBC transaction (commit on success, rollback on failure).
- [ ] Display daily and item-wise sales reports.

## D. Python analysis
- [ ] Connect Python to MySQL and retrieve sales data.
- [ ] Preprocess the retrieved data.
- [ ] Generate item-wise sales chart.
- [ ] Generate daily revenue chart.
- [ ] Generate popular-items chart.
- [ ] Generate stock chart.
- [ ] Save charts in `outputs/`.

## E. Documentation and diagrams
- [ ] Complete `docs/SRS.md`.
- [ ] Create use-case diagram.
- [ ] Create class diagram.
- [ ] Create system architecture diagram.
- [ ] Create ER diagram.
- [ ] Complete `docs/Testing.md` with test cases and actual results.
- [ ] Add screenshots of successful program runs and chart outputs.

## F. Final validation and submission
- [ ] Run `mvn clean compile` without errors.
- [ ] Test every Java feature.
- [ ] Run Python analysis and verify all charts.
- [ ] Verify SQL scripts on the target MySQL database.
- [ ] Update this README with exact run instructions.
- [ ] Push the project to your personal GitHub repository.
- [ ] Submit SRS, diagrams, source code, SQL, Python code, test cases and outputs as required by the assignment.

## Current starting point
The user has already verified:
- Java: 17.0.12
- Maven: 3.10.0
- Maven home: `D:\\apache-maven-3.10.0`
- Project folder: `D:\\College-Canteen-System`

The next step is to create the folders and files from this ZIP inside that project folder, then configure MySQL.
