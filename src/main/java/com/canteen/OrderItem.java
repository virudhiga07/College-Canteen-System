package com.canteen;

import java.math.BigDecimal;

public class OrderItem {
    private int itemId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem(int itemId, int quantity, BigDecimal unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
