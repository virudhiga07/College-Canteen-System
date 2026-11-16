package com.canteen;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private LocalDateTime orderDateTime;
    private BigDecimal totalAmount;

    public Order(int orderId, LocalDateTime orderDateTime, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderDateTime = orderDateTime;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() { return orderId; }
    public LocalDateTime getOrderDateTime() { return orderDateTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
