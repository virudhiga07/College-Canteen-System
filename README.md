# College Canteen Ordering and Sales Analysis System

**Project owner / individual developer:** Virudhiga K V  
**Project type:** Individual academic project  
**Technology:** Java 17, Maven, MySQL, JDBC, Python, Pandas, Matplotlib

## Project scope
This project is for the assigned Scenario 3: College Canteen Ordering and Sales Analysis System.

Required functions:
- Manage food items, prices and available stock.
- Add, update and delete food items.
- Create orders containing one or multiple food items.
- Calculate bills automatically.
- Check stock before confirming an order and update stock after confirmation.
- Store orders and order details in MySQL using transactions.
- Produce daily sales, item-wise sales and popular-item reports.
- Use Python to retrieve and preprocess sales data and create charts for item-wise sales, daily revenue, popular items and stock.

## Folder guide
- `src/main/java/com/canteen/` — Java application source code.
- `src/main/resources/` — application configuration resources.
- `src/test/java/` — Java tests.
- `database/` — SQL schema, sample data and required queries.
- `python/` — Python data retrieval, preprocessing and charts.
- `docs/` — SRS, diagrams and testing documentation.
- `outputs/` — generated chart images and report outputs.

## Setup order
Follow `docs/SETUP_AND_TASKS.md` in order. Do not skip the database setup or the verification steps.

## Run
1. Install JDK 17, Maven, MySQL Server and MySQL Workbench.
2. Create the database by running `database/schema.sql` in MySQL Workbench.
3. Run `database/sample_data.sql`.
4. Update the database username/password in `src/main/java/com/canteen/DatabaseConnection.java`.
5. From this folder, run `mvn clean compile`.
6. Run `mvn exec:java` after the Java application entry point and Maven plugin are configured.
7. Install Python dependencies using `python -m pip install -r python/requirements.txt`.
8. Run `python python/sales_analysis.py` after configuring the database connection in that script.

## Ownership
This is an individual project owned and developed by Virudhiga K V. No company, team, co-owner or collaborator is specified.
