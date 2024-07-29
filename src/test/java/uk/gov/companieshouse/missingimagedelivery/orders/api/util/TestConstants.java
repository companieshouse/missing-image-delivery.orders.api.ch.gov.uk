package uk.gov.companieshouse.missingimagedelivery.orders.api.util;

import static java.util.Collections.singletonList;

public class TestConstants {
    public static final String ERIC_IDENTITY_HEADER_NAME = "ERIC-Identity";
    public static final String REQUEST_ID_VALUE = "f058ebd6-02f7-4d3f-942e-904344e8cde5";
    public static final String ERIC_IDENTITY_VALUE = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";
    public static final String ERIC_IDENTITY_TYPE_OAUTH2_VALUE = "oauth2";
    public static final String ERIC_IDENTITY_TYPE_HEADER_NAME = "ERIC-Identity-Type";
    public static final String ERIC_IDENTITY_TYPE_API_KEY_VALUE = "key";
    public static final String MISSING_IMAGE_DELIVERY_URL = "/orderable/missing-image-deliveries";
    public static final String ERIC_AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    public static final ApiErrorResponsePayload FILING_NOT_FOUND =
        new ApiErrorResponsePayload(singletonList(new Error("ch:service", "filing-history-item-not-found")));
    public static final String FILING_HISTORY_TYPE_CH01 = "CH01";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String DISCOUNT_APPLIED = "0";
    public static final String DISCOUNT_APPLIED_WHEN_ELIGIBLE = "3";
    public static final int    MISSING_IMAGE_DELIVERY_ITEM_COST = 3;
    public static final String MISSING_IMAGE_DELIVERY_ITEM_COST_STRING = "3";
    public static final String POSTAGE_COST = "0";
    public static final String CALCULATED_COST = "3";
    public static final String CALCULATED_COST_AFTER_DISCOUNT = "0";
    public static final String TOTAL_ITEM_COST = "3";

}
