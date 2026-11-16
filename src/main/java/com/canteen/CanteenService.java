package com.canteen;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CanteenService {

    // 1. ADD FOOD ITEM
    public void addFoodItem(String name, double price, int stock) {

        String sql = "INSERT INTO food_items(item_name, price, stock_quantity) VALUES(?,?,?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, stock);

            ps.executeUpdate();

            System.out.println("Food item added successfully.");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. VIEW FOOD ITEMS
    public List<FoodItem> getAllFoodItems() {

        List<FoodItem> items = new ArrayList<>();

        String sql = "SELECT * FROM food_items ORDER BY item_id";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new FoodItem(
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return items;
    }

    public void displayFoodItems() {

        List<FoodItem> items = getAllFoodItems();

        System.out.println("\n---------- FOOD MENU ----------");
        System.out.printf("%-8s %-20s %-12s %-10s%n",
                "ID", "Food Name", "Price", "Stock");

        for (FoodItem item : items) {
            System.out.printf("%-8d %-20s %-12.2f %-10d%n",
                    item.getItemId(),
                    item.getItemName(),
                    item.getPrice(),
                    item.getStockQuantity());
        }

        if (items.isEmpty()) {
            System.out.println("No food items available.");
        }
    }

    // 3. UPDATE FOOD ITEM
    public void updateFoodItem(int id, String name, double price, int stock) {

        String sql = """
            UPDATE food_items
            SET item_name=?, price=?, stock_quantity=?
            WHERE item_id=?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, stock);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Food item updated successfully.");
            } else {
                System.out.println("Food item ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. DELETE FOOD ITEM
    public void deleteFoodItem(int id) {

        String sql = "DELETE FROM food_items WHERE item_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Food item deleted successfully.");
            } else {
                System.out.println("Food item ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Cannot delete this item because it may be used in an existing order.");
        }
    }

    // 5. PLACE ORDER AND GENERATE BILL
    public int placeOrder(Map<Integer, Integer> cart) throws SQLException {

        if (cart.isEmpty()) {
            throw new SQLException("No items selected.");
        }

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                double total = 0;

                // Check food availability and calculate bill
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {

                    int itemId = entry.getKey();
                    int quantity = entry.getValue();

                    if (quantity <= 0) {
                        throw new SQLException("Quantity must be greater than zero.");
                    }

                    String sql = """
                        SELECT item_name, price, stock_quantity
                        FROM food_items WHERE item_id=?
                        """;

                    try (PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setInt(1, itemId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException("Food item ID " + itemId + " not found.");
                            }

                            int stock = rs.getInt("stock_quantity");
                            double price = rs.getDouble("price");

                            if (quantity > stock) {
                                throw new SQLException(
                                    "Insufficient stock for " + rs.getString("item_name"));
                            }

                            total += price * quantity;
                        }
                    }
                }

                // Insert order
                String orderSql =
                    "INSERT INTO orders(order_datetime, total_amount) VALUES(?,?)";

                try (PreparedStatement ps = con.prepareStatement(orderSql)) {

                    ps.setString(1, LocalDateTime.now().toString());
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                }

                // Get generated order ID
                int orderId;

                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {

                    rs.next();
                    orderId = rs.getInt(1);
                }

                // Insert order items and update stock
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {

                    int itemId = entry.getKey();
                    int quantity = entry.getValue();

                    double price;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT price FROM food_items WHERE item_id=?")) {

                        ps.setInt(1, itemId);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            price = rs.getDouble("price");
                        }
                    }

                    String itemSql = """
                        INSERT INTO order_items(order_id, item_id, quantity, unit_price)
                        VALUES(?,?,?,?)
                        """;

                    try (PreparedStatement ps = con.prepareStatement(itemSql)) {

                        ps.setInt(1, orderId);
                        ps.setInt(2, itemId);
                        ps.setInt(3, quantity);
                        ps.setDouble(4, price);

                        ps.executeUpdate();
                    }

                    String stockSql = """
                        UPDATE food_items
                        SET stock_quantity = stock_quantity - ?
                        WHERE item_id=?
                        """;

                    try (PreparedStatement ps = con.prepareStatement(stockSql)) {

                        ps.setInt(1, quantity);
                        ps.setInt(2, itemId);

                        ps.executeUpdate();
                    }
                }

                con.commit();

                System.out.println("\n========== BILL ==========");
                System.out.println("Order ID: " + orderId);

                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT item_name, price FROM food_items WHERE item_id=?")) {

                        ps.setInt(1, entry.getKey());

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                double lineTotal =
                                    rs.getDouble("price") * entry.getValue();

                                System.out.printf("%s x %d = Rs. %.2f%n",
                                    rs.getString("item_name"),
                                    entry.getValue(),
                                    lineTotal);
                            }
                        }
                    }
                }

                System.out.printf("TOTAL AMOUNT: Rs. %.2f%n", total);
                System.out.println("==========================");

                return orderId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // 6. SALES ANALYSIS
    public void displaySalesReport() {

        String summarySql = """
            SELECT COUNT(*) AS total_orders,
                   COALESCE(SUM(total_amount), 0) AS total_sales
            FROM orders
            """;

        String itemSql = """
            SELECT f.item_name,
                   SUM(oi.quantity) AS quantity_sold,
                   SUM(oi.quantity * oi.unit_price) AS item_sales
            FROM order_items oi
            JOIN food_items f ON oi.item_id = f.item_id
            GROUP BY oi.item_id, f.item_name
            ORDER BY quantity_sold DESC
            """;

        String dailySql = """
            SELECT DATE(order_datetime) AS sales_date,
                   COUNT(*) AS orders_count,
                   SUM(total_amount) AS daily_sales
            FROM orders
            GROUP BY DATE(order_datetime)
            ORDER BY sales_date
            """;

        try (Connection con = DatabaseConnection.getConnection()) {

            System.out.println("\n========== SALES REPORT ==========");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(summarySql)) {

                if (rs.next()) {
                    System.out.println("Total Orders: " +
                            rs.getInt("total_orders"));

                    System.out.printf("Total Sales: Rs. %.2f%n",
                            rs.getDouble("total_sales"));
                }
            }

            System.out.println("\n---------- ITEM-WISE SALES ----------");
            System.out.printf("%-20s %-15s %-15s%n",
                    "Food Item", "Quantity Sold", "Sales");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(itemSql)) {

                while (rs.next()) {
                    System.out.printf("%-20s %-15d Rs. %-12.2f%n",
                            rs.getString("item_name"),
                            rs.getInt("quantity_sold"),
                            rs.getDouble("item_sales"));
                }
            }

            System.out.println("\n---------- DAILY SALES ----------");
            System.out.printf("%-20s %-15s %-15s%n",
                    "Date", "Orders", "Sales");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(dailySql)) {

                while (rs.next()) {
                    System.out.printf("%-20s %-15d Rs. %-12.2f%n",
                            rs.getString("sales_date"),
                            rs.getInt("orders_count"),
                            rs.getDouble("daily_sales"));
                }
            }

            System.out.println("==================================");

        } catch (SQLException e) {
            System.out.println("Sales report error: " + e.getMessage());
        }
    }
}