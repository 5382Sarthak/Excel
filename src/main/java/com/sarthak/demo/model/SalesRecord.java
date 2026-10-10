
package com.sarthak.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sales_records")
public class SalesRecord {

    @Id
    private String id;

    private String date;
    private String product;
    private double quantity;
    private double unitPrice;
    private double unitCost;
    private String region;
    private double revenue;
    private double profit;

    public SalesRecord() {
    }

    public SalesRecord(String date, String product, double quantity,
                       double unitPrice, double unitCost, String region,
                       double revenue, double profit) {
        this.date = date;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.region = region;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getId() { return id; }
    public String getDate() { return date; }
    public String getProduct() { return product; }
    public double getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getUnitCost() { return unitCost; }
    public String getRegion() { return region; }
    public double getRevenue() { return revenue; }
    public double getProfit() { return profit; }

    public void setId(String id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setProduct(String product) { this.product = product; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
    public void setRegion(String region) { this.region = region; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public void setProfit(double profit) { this.profit = profit; }
}
