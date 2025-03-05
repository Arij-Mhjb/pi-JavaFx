
package io.github.palexdev.materialfx.demo.model;

public class Product {
    private int id;
    private String nameproduct;
    private double priceproduct;
    private String stock;
    private String category;
    private int quantity;
    private double totalPrice;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNameproduct() { return nameproduct; }
    public void setNameproduct(String nameproduct) { this.nameproduct = nameproduct; }

    public double getPriceproduct() { return priceproduct; }
    public void setPriceproduct(double priceproduct) { this.priceproduct = priceproduct; }

    public String getStock() { return stock; }
    public void setStock(String stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}