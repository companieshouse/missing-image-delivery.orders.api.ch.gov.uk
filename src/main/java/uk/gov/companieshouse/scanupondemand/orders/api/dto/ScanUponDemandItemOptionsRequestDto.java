package uk.gov.companieshouse.scanupondemand.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.constraints.NotEmpty;

@JsonPropertyOrder(alphabetic = true)
public class ScanUponDemandItemOptionsRequestDto {

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
