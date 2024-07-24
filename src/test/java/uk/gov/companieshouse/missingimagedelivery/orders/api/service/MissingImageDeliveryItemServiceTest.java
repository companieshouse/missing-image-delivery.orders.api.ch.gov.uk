package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCosts;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemData;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemOptions;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType;
import uk.gov.companieshouse.missingimagedelivery.orders.api.repository.MissingImageDeliveryItemRepository;
import uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType.MISSING_IMAGE_DELIVERY_MISC;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.DISCOUNT_APPLIED;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.MISSING_IMAGE_DELIVERY_ITEM_COST_STRING;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestUtils.verifyCreationTimestampsWithinExecutionInterval;

/**
 * Unit tests the {@link MissingImageDeliveryItemService} class.
 */
@ExtendWith(MockitoExtension.class)
public class MissingImageDeliveryItemServiceTest {

    private static final String COMPANY_NUMBER = "00000000";
    private static final String DESCRIPTION = "missing image delivery for company 00000000";
    private static final String ID = "SCD-822015-998103";
    private static final String CALCULATED_COST = "10";
    private static final String POSTAGE_COST = "0";
    private static final String FILING_HISTORY_ID = "1";
    private static final String FILING_HISTORY_DATE = "2010-02-12";
    private static final String FILING_HISTORY_DESCRIPTION = "change-person-director-company-with-change-date";
    private static final Map<String, Object> FILING_HISTORY_DESCRIPTION_VALUES = new HashMap<>();
    private static final String FILING_HISTORY_TYPE = "CH01";
    private static final String FILING_HISTORY_BARCODE = "00503271";
    private static final int QUANTITY = 1;
    private static final String KIND = "item#missing-image-delivery";

    public static final String FILING_HISTORY_CATEGORY = "resolution";
    private static final ProductType MISC_PRODUCT_TYPE = MISSING_IMAGE_DELIVERY_MISC;
    private static final boolean USER_ELIGIBLE_FREE_CERTIFICATES = true;
    private static final boolean USER_NOT_ELIGIBLE_FREE_CERTIFICATES = false;

    private static final ItemCosts ITEM_COSTS = new ItemCosts(DISCOUNT_APPLIED, MISSING_IMAGE_DELIVERY_ITEM_COST_STRING,
            CALCULATED_COST, MISSING_IMAGE_DELIVERY_MISC);
    private static final ItemCostCalculation CALCULATION = new ItemCostCalculation(singletonList(ITEM_COSTS),
            POSTAGE_COST, TestConstants.TOTAL_ITEM_COST);

    @InjectMocks
    private MissingImageDeliveryItemService serviceUnderTest;

    @Mock
    private EtagGeneratorService etagGenerator;

    @Mock
    private LinksGeneratorService linksGenerator;

    @Mock
    private IdGeneratorService idGeneratorService;

    @Mock
    private MissingImageDeliveryItemRepository repository;

    @Mock
    private MissingImageDeliveryCostCalculatorService costCalculatorService;

    @Mock
    private DescriptionProviderService descriptionProviderService;

    @Test
    @DisplayName("createMissingImageDeliveryItem creates and saves item with id, timestamps, etag and links, returns item with costs and item options")
    void createMissingImageDeliveryItemPopulatesAndSavesItem() {

        // Given
        when(idGeneratorService.autoGenerateId()).thenReturn(ID);
        final MissingImageDeliveryItemOptions midItemOptions = new MissingImageDeliveryItemOptions(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION, FILING_HISTORY_DESCRIPTION_VALUES, FILING_HISTORY_ID, FILING_HISTORY_TYPE,
                FILING_HISTORY_CATEGORY, FILING_HISTORY_BARCODE);
        when(costCalculatorService.calculateCosts(QUANTITY, MISC_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES)).thenReturn(CALCULATION);
        MissingImageDeliveryItemData missingImageDeliveryItemData = new MissingImageDeliveryItemData();
        missingImageDeliveryItemData.setCompanyNumber(COMPANY_NUMBER);
        MissingImageDeliveryItem missingImageDeliveryItem = new MissingImageDeliveryItem();
        missingImageDeliveryItem.setData(missingImageDeliveryItemData);
        missingImageDeliveryItem.setQuantity(QUANTITY);
        missingImageDeliveryItem.setKind(KIND);
        missingImageDeliveryItem.setItemOptions(midItemOptions);

        when(repository.save(missingImageDeliveryItem)).thenReturn(missingImageDeliveryItem);

        final LocalDateTime intervalStart = LocalDateTime.now();

        // When
        missingImageDeliveryItem = serviceUnderTest.createMissingImageDeliveryItem(missingImageDeliveryItem, USER_NOT_ELIGIBLE_FREE_CERTIFICATES);

        // Then
        final LocalDateTime intervalEnd = LocalDateTime.now();

        verifyCreationTimestampsWithinExecutionInterval(missingImageDeliveryItem, intervalStart, intervalEnd);
        assertThat(missingImageDeliveryItem.getId(), is(ID));
        verify(etagGenerator).generateEtag();
        verify(linksGenerator).generateLinks(ID);
        assertThat(missingImageDeliveryItem.getId(), is(ID));
        verify(costCalculatorService).calculateCosts(QUANTITY, MISC_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES);
        verify(descriptionProviderService).getDescription(COMPANY_NUMBER);
        assertThat(missingImageDeliveryItem.getItemCosts(), is(singletonList(ITEM_COSTS)));
        assertThat(missingImageDeliveryItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(missingImageDeliveryItem.getKind(), is(KIND));
        assertThat(missingImageDeliveryItem.getTotalItemCost(), is(TestConstants.TOTAL_ITEM_COST));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDate(), is(FILING_HISTORY_DATE));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDescription(),
                is(FILING_HISTORY_DESCRIPTION));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDescriptionValues(),
                is(FILING_HISTORY_DESCRIPTION_VALUES));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryId(), is(FILING_HISTORY_ID));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryType(), is(FILING_HISTORY_TYPE));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryCategory(), is(FILING_HISTORY_CATEGORY));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryBarcode(), is(FILING_HISTORY_BARCODE));
        verify(repository).save(missingImageDeliveryItem);
    }


    @Test
    @DisplayName("createMissingImageDeliveryItem creates and saves item with id, timestamps, etag and links, returns item with costs and item options")
    void createMissingImageDeliveryItemHasDiscountForAdmin() {

        // Given
        when(idGeneratorService.autoGenerateId()).thenReturn(ID);
        final MissingImageDeliveryItemOptions midItemOptions = new MissingImageDeliveryItemOptions(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION, FILING_HISTORY_DESCRIPTION_VALUES, FILING_HISTORY_ID, FILING_HISTORY_TYPE,
                FILING_HISTORY_CATEGORY, FILING_HISTORY_BARCODE);
        when(costCalculatorService.calculateCosts(QUANTITY, MISC_PRODUCT_TYPE, USER_ELIGIBLE_FREE_CERTIFICATES)).thenReturn(CALCULATION);
        MissingImageDeliveryItemData missingImageDeliveryItemData = new MissingImageDeliveryItemData();
        missingImageDeliveryItemData.setCompanyNumber(COMPANY_NUMBER);
        MissingImageDeliveryItem missingImageDeliveryItem = new MissingImageDeliveryItem();
        missingImageDeliveryItem.setData(missingImageDeliveryItemData);
        missingImageDeliveryItem.setQuantity(QUANTITY);
        missingImageDeliveryItem.setKind(KIND);
        missingImageDeliveryItem.setItemOptions(midItemOptions);

        when(repository.save(missingImageDeliveryItem)).thenReturn(missingImageDeliveryItem);

        final LocalDateTime intervalStart = LocalDateTime.now();

        // When
        missingImageDeliveryItem = serviceUnderTest.createMissingImageDeliveryItem(missingImageDeliveryItem, USER_ELIGIBLE_FREE_CERTIFICATES);

        // Then
        final LocalDateTime intervalEnd = LocalDateTime.now();

        verifyCreationTimestampsWithinExecutionInterval(missingImageDeliveryItem, intervalStart, intervalEnd);
        assertThat(missingImageDeliveryItem.getId(), is(ID));
        verify(etagGenerator).generateEtag();
        verify(linksGenerator).generateLinks(ID);
        assertThat(missingImageDeliveryItem.getId(), is(ID));
        verify(costCalculatorService).calculateCosts(QUANTITY, MISC_PRODUCT_TYPE, USER_ELIGIBLE_FREE_CERTIFICATES);
        verify(descriptionProviderService).getDescription(COMPANY_NUMBER);
        assertThat(missingImageDeliveryItem.getItemCosts(), is(singletonList(ITEM_COSTS)));
        assertThat(missingImageDeliveryItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(missingImageDeliveryItem.getKind(), is(KIND));
        assertThat(missingImageDeliveryItem.getTotalItemCost(), is(TestConstants.TOTAL_ITEM_COST));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDate(), is(FILING_HISTORY_DATE));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDescription(),
                is(FILING_HISTORY_DESCRIPTION));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryDescriptionValues(),
                is(FILING_HISTORY_DESCRIPTION_VALUES));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryId(), is(FILING_HISTORY_ID));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryType(), is(FILING_HISTORY_TYPE));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryCategory(), is(FILING_HISTORY_CATEGORY));
        assertThat(missingImageDeliveryItem.getItemOptions().getFilingHistoryBarcode(), is(FILING_HISTORY_BARCODE));
        verify(repository).save(missingImageDeliveryItem);
    }

}
