package uk.gov.companieshouse.missingimagedelivery.orders.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class ItemCosts {
    @JsonProperty("discount_applied")
    private String discountApplied;

    @JsonProperty("item_cost")
    private String itemCost;

    @JsonProperty("calculated_cost")
    private String calculatedCost;

    @JsonProperty("product_type")
    private ProductType productType;

    public ItemCosts() {
    }

    public ItemCosts(String discountApplied,
                     String itemCost,
                     String calculatedCost,
                     ProductType productType) {
        this.discountApplied = discountApplied;
        this.itemCost = itemCost;
        this.calculatedCost = calculatedCost;
        this.productType = productType;
    }

    public String getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(String discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getItemCost() {
        return itemCost;
    }

    public void setItemCost(String itemCost) {
        this.itemCost = itemCost;
    }

    public String getCalculatedCost() {
        return calculatedCost;
    }

    public void setCalculatedCost(String calculatedCost) {
        this.calculatedCost = calculatedCost;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
