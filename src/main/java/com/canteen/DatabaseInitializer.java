package com.canteen;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void createTables() {

        String foodTable = """
            CREATE TABLE IF NOT EXISTS food_items (
                item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_name TEXT NOT NULL,
                price REAL NOT NULL,
                stock_quantity INTEGER NOT NULL
            )
            """;

        String orderTable = """
            CREATE TABLE IF NOT EXISTS orders (
                order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_datetime TEXT NOT NULL,
                total_amount REAL NOT NULL
            )
            """;

        String orderItemsTable = """
            CREATE TABLE IF NOT EXISTS order_items (
                order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER,
                item_id INTEGER,
                quantity INTEGER NOT NULL,
                unit_price REAL NOT NULL,
                FOREIGN KEY(order_id) REFERENCES orders(order_id),
                FOREIGN KEY(item_id) REFERENCES food_items(item_id)
            )
            """;

        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute(foodTable);
            stmt.execute(orderTable);
            stmt.execute(orderItemsTable);

            System.out.println("Database tables are ready.");

        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}