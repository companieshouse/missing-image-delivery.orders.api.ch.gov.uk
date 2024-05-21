package uk.gov.companieshouse.missingimagedelivery.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * An instance of this represents the JSON serializable certificate item for use
 * in REST requests and responses.
 */
@JsonPropertyOrder(alphabetic = true)
public class MissingImageDeliveryItemRequestDTO {

    @NotNull
    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("customer_reference")
    private String customerReference;

    @Valid
    @NotNull
    @JsonProperty("item_options")
    private MissingImageDeliveryItemOptionsRequestDto itemOptions;

    @NotNull
    @JsonProperty("quantity")
    private Integer quantity;

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public MissingImageDeliveryItemOptionsRequestDto getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(MissingImageDeliveryItemOptionsRequestDto itemOptions) {
        this.itemOptions = itemOptions;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
