
package com.canteen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class WebServer {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        initializeDatabase();

        int port = Integer.parseInt(
    System.getenv().getOrDefault("PORT", "8080")
);

HttpServer server = HttpServer.create(
    new InetSocketAddress("0.0.0.0", port), 0
);
        server.createContext("/api/", WebServer::handleApi);
        server.createContext("/", WebServer::handleStatic);

        server.start();

        System.out.println("==================================");
        System.out.println(" VCET COLLEGE CANTEEN SYSTEM");
        System.out.println(" Website: http://localhost:8080");
        System.out.println("==================================");
    }

    // DATABASE INITIALIZATION
    private static void initializeDatabase() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS food_items (
                    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL,
                    price REAL NOT NULL,
                    stock_quantity INTEGER NOT NULL DEFAULT 0
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_datetime TEXT NOT NULL,
                    total_amount REAL NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL
                )
            """);

            // Add category column to an existing database if needed.
            boolean categoryExists = false;

            try (ResultSet rs = st.executeQuery(
                    "PRAGMA table_info(food_items)")) {
                while (rs.next()) {
                    if ("category".equalsIgnoreCase(rs.getString("name"))) {
                        categoryExists = true;
                        break;
                    }
                }
            }

            if (!categoryExists) {
                st.execute("""
                    ALTER TABLE food_items
                    ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'
                """);
            }

            // Insert sample foods only when the menu is empty.
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM food_items")) {

                if (rs.next() && rs.getInt(1) == 0) {
                    insertSampleFoods(con);
                }
            }
        }

        System.out.println("Database is ready.");
    }

    private static void insertSampleFoods(Connection con)
            throws SQLException {

        String[][] foods = {
            {"Chicken Rice", "110", "Rice & Biryani"},
            {"Chicken Biryani", "140", "Rice & Biryani"},
            {"Chicken Kothu", "100", "Chicken & Kothu"},
            {"Chilli Chicken", "120", "Chicken & Kothu"},
            {"Chilli Parotta", "80", "Tiffin"},
            {"Parotta", "15", "Tiffin"},
            {"Dosa", "35", "Tiffin"},
            {"Veg Rice", "80", "Rice & Biryani"},
            {"Noodles", "80", "Fast Food"},
            {"Samosa", "15", "Snacks"},
            {"Egg Puffs", "20", "Puffs & Rolls"},
            {"Mushroom Puffs", "25", "Puffs & Rolls"},
            {"Chicken Roll", "50", "Puffs & Rolls"},
            {"Veg Roll", "35", "Puffs & Rolls"},
            {"Brownie", "45", "Desserts"},
            {"Piece Cake", "40", "Desserts"},
            {"Cookie Pie", "50", "Desserts"},
            {"Sandwich", "50", "Fast Food"},
            {"Vada", "10", "Snacks"},
            {"Bonda", "10", "Snacks"},
            {"Juice", "35", "Beverages"},
            {"Jam Bun", "20", "Bakery"},
            {"Tea", "12", "Beverages"},
            {"Coffee", "18", "Beverages"},
            {"Pani Puri", "30", "Street Food"},
            {"Masala Puri", "40", "Street Food"},
            {"Kaalan", "50", "Street Food"}
        };

        String sql = """
            INSERT INTO food_items
            (item_name, price, stock_quantity, category)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] food : foods) {
                ps.setString(1, food[0]);
                ps.setDouble(2, Double.parseDouble(food[1]));
                ps.setInt(3, 50);
                ps.setString(4, food[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // API ROUTER
    private static void handleApi(HttpExchange exchange)
            throws IOException {

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/api/foods")) {
                if (method.equals("GET")) {
                    sendJson(exchange, 200, getFoods());
                } else if (method.equals("POST")) {
                    addFood(exchange);
                } else {
                    sendJson(exchange, 405, error("Method not allowed"));
                }

            } else if (path.startsWith("/api/foods/")) {
                int id = Integer.parseInt(
                        path.substring("/api/foods/".length()));

                if (method.equals("PUT")) {
                    updateFood(exchange, id);
                } else if (method.equals("DELETE")) {
                    deleteFood(exchange, id);
                } else {
                    sendJson(exchange, 405, error("Method not allowed"));
                }

            } else if (path.equals("/api/orders")) {
                if (method.equals("GET")) {
                    sendJson(exchange, 200, getOrders());
                } else if (method.equals("POST")) {
                    placeOrder(exchange);
                } else {
                    sendJson(exchange, 405, error("Method not allowed"));
                }

            } else if (path.equals("/api/sales")
                    && method.equals("GET")) {
                sendJson(exchange, 200, getSales());

            } else {
                sendJson(exchange, 404, error("API not found"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, error(e.getMessage()));
        }
    }

    // GET FOOD ITEMS
    private static List<Map<String, Object>> getFoods()
            throws SQLException {

        List<Map<String, Object>> foods = new ArrayList<>();

        String sql = "SELECT * FROM food_items ORDER BY item_id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> food = new LinkedHashMap<>();

                food.put("itemId", rs.getInt("item_id"));
                food.put("itemName", rs.getString("item_name"));
                food.put("price", rs.getDouble("price"));
                food.put("stockQuantity",
                        rs.getInt("stock_quantity"));
                food.put("category", rs.getString("category"));

                foods.add(food);
            }
        }

        return foods;
    }

    // ADD FOOD ITEM
    private static void addFood(HttpExchange exchange)
            throws Exception {

        JsonObject data = readBody(exchange);

        String sql = """
            INSERT INTO food_items
            (item_name, price, stock_quantity, category)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, data.get("itemName").getAsString());
            ps.setDouble(2, data.get("price").getAsDouble());
            ps.setInt(3, data.get("stockQuantity").getAsInt());
            ps.setString(4, data.get("category").getAsString());

            ps.executeUpdate();
        }

        sendJson(exchange, 200, message("Food added successfully"));
    }

    // UPDATE FOOD ITEM
    private static void updateFood(HttpExchange exchange, int id)
            throws Exception {

        JsonObject data = readBody(exchange);

        String sql = """
            UPDATE food_items
            SET item_name = ?, price = ?,
                stock_quantity = ?, category = ?
            WHERE item_id = ?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, data.get("itemName").getAsString());
            ps.setDouble(2, data.get("price").getAsDouble());
            ps.setInt(3, data.get("stockQuantity").getAsInt());
            ps.setString(4, data.get("category").getAsString());
            ps.setInt(5, id);

            ps.executeUpdate();
        }

        sendJson(exchange, 200, message("Food updated successfully"));
    }

    // DELETE FOOD ITEM
    private static void deleteFood(HttpExchange exchange, int id)
            throws Exception {

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM food_items WHERE item_id = ?")) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                sendJson(exchange, 404, error("Food item not found"));
                return;
            }
        }

        sendJson(exchange, 200, message("Food deleted successfully"));
    }

    // PLACE ORDER AND UPDATE STOCK
    private static void placeOrder(HttpExchange exchange)
            throws Exception {

        JsonObject data = readBody(exchange);

        if (!data.has("items") || !data.get("items").isJsonArray()
                || data.getAsJsonArray("items").isEmpty()) {
            sendJson(exchange, 400, error("Cart is empty"));
            return;
        }

        Map<Integer, Integer> cart = new LinkedHashMap<>();

        for (var element : data.getAsJsonArray("items")) {
            JsonObject item = element.getAsJsonObject();
            int id = item.get("itemId").getAsInt();
            int quantity = item.get("quantity").getAsInt();

            if (id <= 0 || quantity <= 0) {
                sendJson(exchange, 400, error("Invalid item or quantity"));
                return;
            }

            cart.merge(id, quantity, Integer::sum);
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                double total = 0;
                List<Map<String, Object>> billItems = new ArrayList<>();

                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    int id = entry.getKey();
                    int quantity = entry.getValue();

                    String sql = """
                        SELECT item_name, price, stock_quantity
                        FROM food_items WHERE item_id = ?
                    """;

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, id);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new Exception("Food item not found");
                            }

                            int stock = rs.getInt("stock_quantity");

                            if (stock < quantity) {
                                throw new Exception(
                                        "Not enough stock for "
                                        + rs.getString("item_name"));
                            }

                            double price = rs.getDouble("price");
                            double subtotal = price * quantity;
                            total += subtotal;

                            Map<String, Object> billItem =
                                    new LinkedHashMap<>();

                            billItem.put("itemId", id);
                            billItem.put("itemName",
                                    rs.getString("item_name"));
                            billItem.put("quantity", quantity);
                            billItem.put("price", price);
                            billItem.put("subtotal", subtotal);

                            billItems.add(billItem);
                        }
                    }
                }

                String orderSql = """
                    INSERT INTO orders (order_datetime, total_amount)
                    VALUES (?, ?)
                """;

                try (PreparedStatement ps = con.prepareStatement(orderSql)) {
                    ps.setString(1, LocalDateTime.now().toString());
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                }

                int orderId;

                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT last_insert_rowid()")) {
                    rs.next();
                    orderId = rs.getInt(1);
                }

                for (Map<String, Object> item : billItems) {
                    int id = (int) item.get("itemId");
                    int quantity = (int) item.get("quantity");
                    double price = (double) item.get("price");

                    try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO order_items
                        (order_id, item_id, quantity, unit_price)
                        VALUES (?, ?, ?, ?)
                    """)) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, id);
                        ps.setInt(3, quantity);
                        ps.setDouble(4, price);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE food_items
                        SET stock_quantity = stock_quantity - ?
                        WHERE item_id = ?
                    """)) {
                        ps.setInt(1, quantity);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                }

                con.commit();

                Map<String, Object> bill = new LinkedHashMap<>();
                bill.put("orderId", orderId);
                bill.put("orderDate", LocalDateTime.now().toString());
                bill.put("items", billItems);
                bill.put("total", total);

                sendJson(exchange, 200, bill);

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    // GET ORDERS
    private static List<Map<String, Object>> getOrders()
            throws SQLException {

        List<Map<String, Object>> orders = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM orders ORDER BY order_id DESC")) {

            while (rs.next()) {
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("orderId", rs.getInt("order_id"));
                order.put("orderDate", rs.getString("order_datetime"));
                order.put("total", rs.getDouble("total_amount"));
                orders.add(order);
            }
        }

        return orders;
    }

    // SALES ANALYSIS
    private static Map<String, Object> getSales()
            throws SQLException {

        Map<String, Object> sales = new LinkedHashMap<>();

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery("""
                SELECT COUNT(*) AS total_orders,
                       COALESCE(SUM(total_amount), 0) AS total_sales
                FROM orders
            """)) {
                rs.next();
                sales.put("totalOrders", rs.getInt("total_orders"));
                sales.put("totalSales", rs.getDouble("total_sales"));
            }

            try (ResultSet rs = st.executeQuery("""
                SELECT f.item_name,
                       SUM(oi.quantity) AS quantity_sold,
                       SUM(oi.quantity * oi.unit_price) AS revenue
                FROM order_items oi
                JOIN food_items f ON oi.item_id = f.item_id
                GROUP BY oi.item_id, f.item_name
                ORDER BY revenue DESC
            """)) {
                List<Map<String, Object>> items = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("itemName", rs.getString("item_name"));
                    item.put("quantitySold",
                            rs.getInt("quantity_sold"));
                    item.put("revenue", rs.getDouble("revenue"));
                    items.add(item);
                }

                sales.put("items", items);
            }
        }

        return sales;
    }

    // SERVE FRONTEND FILES
    private static void handleStatic(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        String resource = "/static" + path;

        try (InputStream input =
                     WebServer.class.getResourceAsStream(resource)) {

            if (input == null) {
                sendText(exchange, 404, "Page not found");
                return;
            }

            byte[] bytes = input.readAllBytes();

            String type = "text/plain";

            if (path.endsWith(".html")) {
                type = "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                type = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                type = "application/javascript; charset=UTF-8";
            }

            exchange.getResponseHeaders().set(
                    "Content-Type", type);

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    // HELPER METHODS
    private static JsonObject readBody(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return JsonParser.parseString(body).getAsJsonObject();
    }

    private static void sendJson(
            HttpExchange exchange, int status, Object data)
            throws IOException {

        byte[] bytes = gson.toJson(data)
                .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void sendText(
            HttpExchange exchange, int status, String message)
            throws IOException {

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static Map<String, String> error(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("error", message == null ? "Unknown error" : message);
        return result;
    }

    private static Map<String, String> message(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
