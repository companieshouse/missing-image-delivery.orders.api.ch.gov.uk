package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.missingimagedelivery.orders.api.config.CostsConfig;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCosts;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType.MISSING_IMAGE_DELIVERY_ACCOUNTS;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.*;

/**
 * Unit tests the {@link MissingImageDeliveryCostCalculatorService} class.
 */
@ExtendWith(MockitoExtension.class)
class MissingImageDeliveryCostCalculatorServiceTest {

    private static final String MVP_TOTAL_ITEM_COST = "3";
    private static final String POST_MVP_TOTAL_ITEM_COST = "15";
    private static final int ADJUSTED_MISSING_IMAGE_DELIVERY_ITEM_COST = 5;
    private static final String ADJUSTED_MISSING_IMAGE_DELIVERY_ITEM_COST_STRING = "5";
    private static final String ADJUSTED_ITEM_COST_CALCULATED_COST = "5";
    private static final String ADJUSTED_ITEM_COST_TOTAL_ITEM_COST = "5";

    private static final int BASIC_COST_DISCOUNT = 0;
    private static final String DISCOUNT_ITEM_COST_STRING = "0";

    private static final int MVP_QUANTITY = 1;
    private static final int POST_MVP_QUANTITY = 5;
    private static final int INVALID_QUANTITY = 0;

    private static final boolean USER_ELIGIBLE_FREE_CERTIFICATES = true;
    private static final boolean USER_NOT_ELIGIBLE_FREE_CERTIFICATES = false;

    private static final ProductType ACCOUNTS_PRODUCT_TYPE = MISSING_IMAGE_DELIVERY_ACCOUNTS;

    private static final ItemCostCalculation MVP_EXPECTED_CALCULATION = new ItemCostCalculation(
            singletonList(new ItemCosts(DISCOUNT_APPLIED,
                    MISSING_IMAGE_DELIVERY_ITEM_COST_STRING,
                    CALCULATED_COST,
                    MISSING_IMAGE_DELIVERY_ACCOUNTS)),
            POSTAGE_COST,
            MVP_TOTAL_ITEM_COST);

    private static final ItemCostCalculation POST_MVP_EXPECTED_CALCULATION = new ItemCostCalculation(
            singletonList(new ItemCosts(DISCOUNT_APPLIED,
                    MISSING_IMAGE_DELIVERY_ITEM_COST_STRING,
                    CALCULATED_COST,
                    MISSING_IMAGE_DELIVERY_ACCOUNTS)),
            POSTAGE_COST,
            POST_MVP_TOTAL_ITEM_COST);

    private static final ItemCostCalculation ADJUSTED_ITEM_COST_EXPECTED_CALCULATION = new ItemCostCalculation(
            singletonList(new ItemCosts(DISCOUNT_APPLIED,
                    ADJUSTED_MISSING_IMAGE_DELIVERY_ITEM_COST_STRING,
                    ADJUSTED_ITEM_COST_CALCULATED_COST,
                    MISSING_IMAGE_DELIVERY_ACCOUNTS)),
            POSTAGE_COST,
            ADJUSTED_ITEM_COST_TOTAL_ITEM_COST);

    private static final ItemCostCalculation FULL_DISCOUNT_EXPECTED_CALCULATION = new ItemCostCalculation(
            singletonList(new ItemCosts(
                    DISCOUNT_APPLIED, // Full discount amount, or you may use "0" depending on the logic
                    DISCOUNT_ITEM_COST_STRING,
                    "0",  // Should be "0" indicating the item cost after discount
                    ACCOUNTS_PRODUCT_TYPE)),
            POSTAGE_COST,  // Assuming no postage cost with full discount
            FULL_DISCOUNT_EXPECTED_COST);

    @InjectMocks
    private MissingImageDeliveryCostCalculatorService serviceUnderTest;

    @Mock
    private CostsConfig costs;

    @Test
    @DisplayName("calculateCosts produces expected results for MVP quantity")
    void calculateCostsProducesExpectedResultsForMvpQuantity() {
        when(costs.getMissingImageDeliveryItemCost()).thenReturn(MISSING_IMAGE_DELIVERY_ITEM_COST);
        assertThat(serviceUnderTest.calculateCosts(MVP_QUANTITY, ACCOUNTS_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES)).
                isEqualToComparingFieldByFieldRecursively(MVP_EXPECTED_CALCULATION);
    }

    @Test
    @DisplayName("calculateCosts produces expected results for a post MVP quantity")
    void calculateCostsProducesExpectedResultsForPostMvpQuantity() {
        when(costs.getMissingImageDeliveryItemCost()).thenReturn(MISSING_IMAGE_DELIVERY_ITEM_COST);
        assertThat(serviceUnderTest.calculateCosts(POST_MVP_QUANTITY, ACCOUNTS_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES))
                .isEqualToComparingFieldByFieldRecursively(POST_MVP_EXPECTED_CALCULATION);
    }

    @Test
    @DisplayName("Incorrect quantity results in IllegalArgumentException")
    void incorrectQuantityTriggersIllegalArgumentException() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> serviceUnderTest.calculateCosts(INVALID_QUANTITY, ACCOUNTS_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES));
        MatcherAssert.assertThat(exception.getMessage(), is("quantity must be greater than or equal to 1!"));
    }

    @Test
    @DisplayName("Calculated cost should be the item cost")
    void calculatedCostIsItemCost() {
        when(costs.getMissingImageDeliveryItemCost()).thenReturn(ADJUSTED_MISSING_IMAGE_DELIVERY_ITEM_COST);
        assertThat(serviceUnderTest.calculateCosts(MVP_QUANTITY, ACCOUNTS_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES)).
                isEqualToComparingFieldByFieldRecursively(ADJUSTED_ITEM_COST_EXPECTED_CALCULATION);
    }


    @Test
    @DisplayName("Calculated cost should be 0 when user is eligible for free certificates")
    void calculatedCostIs0WithDiscount() {
        when(costs.getMissingImageDeliveryItemCost()).thenReturn(BASIC_COST_DISCOUNT);
        ItemCostCalculation result = serviceUnderTest.calculateCosts(
                MVP_QUANTITY,
                ACCOUNTS_PRODUCT_TYPE,
                USER_ELIGIBLE_FREE_CERTIFICATES
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(FULL_DISCOUNT_EXPECTED_CALCULATION);
    }


    @DisplayName("calculateCosts produces expected results for a post-MVP quantity with full discount")
    @Test
    void calculateCostsProducesExpectedResultsForPostMvpQuantityWithFullDiscount() {
        when(costs.getMissingImageDeliveryItemCost()).thenReturn(BASIC_COST_DISCOUNT);
        ItemCostCalculation result = serviceUnderTest.calculateCosts(
                POST_MVP_QUANTITY,
                ACCOUNTS_PRODUCT_TYPE,
                USER_ELIGIBLE_FREE_CERTIFICATES
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(FULL_DISCOUNT_EXPECTED_CALCULATION);
    }
}




