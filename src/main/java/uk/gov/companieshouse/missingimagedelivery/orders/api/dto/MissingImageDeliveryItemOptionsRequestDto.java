package uk.gov.companieshouse.missingimagedelivery.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotEmpty;

@JsonPropertyOrder(alphabetic = true)
public class MissingImageDeliveryItemOptionsRequestDto {

    @NotEmpty
    @JsonProperty("filing_history_id")
    private String filingHistoryId;

    public String getFilingHistoryId() {
        return filingHistoryId;
    }

    public void setFilingHistoryId(String filingHistoryId) {
        this.filingHistoryId = filingHistoryId;
    }

}
