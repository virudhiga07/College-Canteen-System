package com.canteen;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static CanteenService service = new CanteenService();

    public static void main(String[] args) {

        DatabaseInitializer.createTables();

        int choice;

        do {
            System.out.println("\n==================================");
            System.out.println("   COLLEGE CANTEEN MANAGEMENT");
            System.out.println("==================================");
            System.out.println("1. Add Food Item");
            System.out.println("2. View Food Items");
            System.out.println("3. Update Food Item");
            System.out.println("4. Delete Food Item");
            System.out.println("5. Place Order and Generate Bill");
            System.out.println("6. View Sales Analysis");
            System.out.println("7. Exit");
            System.out.println("==================================");

            choice = readInt("Enter your choice: ");

            switch (choice) {

                case 1:
                    addFood();
                    break;

                case 2:
                    service.displayFoodItems();
                    break;

                case 3:
                    updateFood();
                    break;

                case 4:
                    deleteFood();
                    break;

                case 5:
                    placeOrder();
                    break;

                case 6:
                    service.displaySalesReport();
                    break;

                case 7:
                    System.out.println("Thank you for using the Canteen System!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 7);

        sc.close();
    }

    // ADD FOOD
    static void addFood() {

        System.out.println("\n--- Add Food Item ---");

        String name = readText("Enter food name: ");
        double price = readDouble("Enter price: ");
        int stock = readInt("Enter stock quantity: ");

        if (name.isBlank() || price <= 0 || stock < 0) {
            System.out.println("Enter a valid name, positive price and non-negative stock.");
            return;
        }

        service.addFoodItem(name, price, stock);
    }

    // UPDATE FOOD
    static void updateFood() {

        System.out.println("\n--- Update Food Item ---");

        service.displayFoodItems();

        int id = readInt("Enter food item ID: ");
        String name = readText("Enter new food name: ");
        double price = readDouble("Enter new price: ");
        int stock = readInt("Enter new stock quantity: ");

        if (name.isBlank() || price <= 0 || stock < 0) {
            System.out.println("Enter valid details.");
            return;
        }

        service.updateFoodItem(id, name, price, stock);
    }

    // DELETE FOOD
    static void deleteFood() {

        System.out.println("\n--- Delete Food Item ---");

        service.displayFoodItems();

        int id = readInt("Enter food item ID to delete: ");

        service.deleteFoodItem(id);
    }

    // PLACE ORDER
    static void placeOrder() {

        System.out.println("\n--- Place Customer Order ---");

        service.displayFoodItems();

        Map<Integer, Integer> cart = new LinkedHashMap<>();

        System.out.println("\nEnter food item IDs and quantities.");
        System.out.println("Enter 0 as the item ID to finish.");

        while (true) {

            int id = readInt("Enter food item ID: ");

            if (id == 0) {
                break;
            }

            int quantity = readInt("Enter quantity: ");

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                continue;
            }

            cart.merge(id, quantity, Integer::sum);
            System.out.println("Item added to order.");
        }

        if (cart.isEmpty()) {
            System.out.println("No items selected.");
            return;
        }

        try {
            service.placeOrder(cart);

        } catch (SQLException e) {
            System.out.println("Order failed: " + e.getMessage());
        }
    }

    // INPUT METHODS
    static int readInt(String message) {

        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    static double readDouble(String message) {

        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(sc.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid price.");
            }
        }
    }

    static String readText(String message) {

        System.out.print(message);
        return sc.nextLine().trim();
    }
}