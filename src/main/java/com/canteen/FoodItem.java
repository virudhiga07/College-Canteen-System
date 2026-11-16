package com.canteen;

public class FoodItem {
    private int itemId;
    private String itemName;
    private double price;
    private int stockQuantity;

    public FoodItem(int itemId, String itemName, double price, int stockQuantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}
