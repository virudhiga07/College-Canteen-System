USE college_canteen;

-- Display menu and available stock
SELECT * FROM food_items;

-- Item-wise quantity sold and revenue
SELECT f.item_name, SUM(oi.quantity) AS quantity_sold,
       SUM(oi.line_total) AS total_revenue
FROM order_items oi
JOIN food_items f ON f.item_id = oi.item_id
GROUP BY f.item_id, f.item_name
ORDER BY total_revenue DESC;

-- Daily revenue
SELECT DATE(order_datetime) AS sales_date, SUM(total_amount) AS daily_revenue
FROM orders
GROUP BY DATE(order_datetime)
ORDER BY sales_date;

-- Popular items
SELECT f.item_name, SUM(oi.quantity) AS quantity_sold
FROM order_items oi
JOIN food_items f ON f.item_id = oi.item_id
GROUP BY f.item_id, f.item_name
ORDER BY quantity_sold DESC;
