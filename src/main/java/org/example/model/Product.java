package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {

    private int id;
    private String name;
    private String sku;
    private BigDecimal unitPrice;
    private int minimumStockLevel;
    private LocalDate expiryDate;

    public Product(String name, String sku, BigDecimal unitPrice,
                   int minimumStockLevel, LocalDate expiryDate) {
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.minimumStockLevel = minimumStockLevel;
        this.expiryDate = expiryDate;
    }

    public Product(int id, String name, String sku,
                   BigDecimal unitPrice, int minimumStockLevel,
                   LocalDate expiryDate) {

        this(name, sku, unitPrice, minimumStockLevel, expiryDate);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}