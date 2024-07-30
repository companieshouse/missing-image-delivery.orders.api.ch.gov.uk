package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.missingimagedelivery.orders.api.config.CostsConfig;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCosts;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType;

import static java.util.Collections.singletonList;

/**
 * Simple service class implemented for consistency across the three Item API implementations.
 */
@Service
public class MissingImageDeliveryCostCalculatorService {

    private static final String ZERO_POSTAGE_COST = "0";      // Postage is not applicable to MID.

    private final CostsConfig costs;
    /**
     * Constructor.
     * @param costs the configured costs used by this in its calculations
     */
    public MissingImageDeliveryCostCalculatorService(final CostsConfig costs) { this.costs = costs; }

    /**
     * Calculates the missing image delivery item costs.
     *
     * @param quantity                 the number of items
     * @param productType              product type based on category
     * @param userGetsFreeCertificates if user has permission admin/free-mids, return true
     * @return all of the relevant costs
     */
    public ItemCostCalculation calculateCosts(final int quantity, final ProductType productType, final boolean userGetsFreeCertificates) {
        checkArguments(quantity);
        final int basicCost = costs.getMissingImageDeliveryItemCost();
        final int calculatedCost;
        if (userGetsFreeCertificates) {
            calculatedCost = 0;
        } else {
            calculatedCost = basicCost;
        }
        // If the user has permission, discount is calculated which is based on the total basic cost so no pay is needed
        final String discountApplied = userGetsFreeCertificates ? Integer.toString(basicCost) : "0";
        final String itemCost = Integer.toString(basicCost);
        final String totalItemCost = Integer.toString(quantity * calculatedCost);
        return new ItemCostCalculation(
                singletonList(new ItemCosts(discountApplied,
                        itemCost,
                        Integer.toString(calculatedCost),
                        productType)),
                ZERO_POSTAGE_COST,
                totalItemCost);
    }

    /**
     * Utility method that checks the arguments provided to it. Throws an {@link IllegalArgumentException} should these
     * be outside of the range of reasonable values.
     * @param quantity the quantity of certificate items specified
     */
    private void checkArguments(final int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1!");
        }
    }

}

