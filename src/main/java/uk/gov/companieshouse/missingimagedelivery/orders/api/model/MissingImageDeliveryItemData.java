package uk.gov.companieshouse.missingimagedelivery.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * An instance of this represents an item of any type.
 */
public class MissingImageDeliveryItemData {

    // Postal delivery is not applicable to MID.
    private static final boolean NO_POSTAL_DELIVERY = false;

    @Field("id")
    private String id;

    private String companyName;

    private String companyNumber;

    private String customerReference;

    private String description;

    private String descriptionIdentifier;

    private Map<String, String> descriptionValues;

    private String etag;

    private List<ItemCosts> itemCosts;

    private MissingImageDeliveryItemOptions itemOptions;

    private String kind;

    private Links links;

    private String postageCost;

    private Boolean isPostalDelivery = NO_POSTAL_DELIVERY;

    private Integer quantity;

    private String totalItemCost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionIdentifier() {
        return descriptionIdentifier;
    }

    public void setDescriptionIdentifier(String descriptionIdentifier) {
        this.descriptionIdentifier = descriptionIdentifier;
    }

    public Map<String, String> getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(Map<String, String> descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public List<ItemCosts> getItemCosts() { return itemCosts; }

    public void setItemCosts(List<ItemCosts> itemCosts) { this.itemCosts = itemCosts; }

    public MissingImageDeliveryItemOptions getItemOptions() { return itemOptions; }

    public void setItemOptions(MissingImageDeliveryItemOptions itemOptions) { this.itemOptions = itemOptions; }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getPostageCost() {
        return postageCost;
    }

    public void setPostageCost(String postageCost) {
        this.postageCost = postageCost;
    }

    public Boolean isPostalDelivery() {
        return isPostalDelivery;
    }

    public void setPostalDelivery(boolean postalDelivery) {
        this.isPostalDelivery = postalDelivery;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

	public String getTotalItemCost() {
		return totalItemCost;
	}

	public void setTotalItemCost(String totalItemCost) {
		this.totalItemCost = totalItemCost;
	}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
