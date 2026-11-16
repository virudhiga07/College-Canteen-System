# Configure the database connection, retrieve sales data,
# preprocess it with Pandas, and save the required charts in ../outputs/.
# Install dependencies with: python -m pip install -r python/requirements.txt

from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt
import mysql.connector

OUTPUT_DIR = Path(__file__).resolve().parent.parent / "outputs"
OUTPUT_DIR.mkdir(exist_ok=True)

# Replace with your local MySQL credentials.
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "YOUR_MYSQL_PASSWORD",
    "database": "college_canteen",
}

def main():
    connection = mysql.connector.connect(**DB_CONFIG)
    try:
        query = """
            SELECT f.item_name, oi.quantity, oi.line_total,
                   DATE(o.order_datetime) AS sales_date
            FROM order_items oi
            JOIN food_items f ON f.item_id = oi.item_id
            JOIN orders o ON o.order_id = oi.order_id
        """
        data = pd.read_sql(query, connection)
        print(data.head())
        print("Retrieved rows:", len(data))
        # Add item-wise, daily-revenue, popular-item and stock charts
        # after order data is available.
    finally:
        connection.close()

if __name__ == "__main__":
    main()
