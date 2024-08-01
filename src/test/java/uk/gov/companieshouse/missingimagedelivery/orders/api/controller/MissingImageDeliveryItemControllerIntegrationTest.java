package uk.gov.companieshouse.missingimagedelivery.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.missingimagedelivery.orders.api.config.AbstractMongoConfig;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemOptionsRequestDto;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemRequestDTO;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemResponseDTO;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCosts;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.Links;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemData;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemOptions;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType;
import uk.gov.companieshouse.missingimagedelivery.orders.api.repository.MissingImageDeliveryItemRepository;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.ApiClientService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.CompanyService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.EtagGeneratorService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.IdGeneratorService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.MissingImageDeliveryCostCalculatorService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyZeroInteractions;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType.MISSING_IMAGE_DELIVERY_ACCOUNTS;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.CALCULATED_COST;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.DISCOUNT_APPLIED;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_OAUTH2_VALUE;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.MISSING_IMAGE_DELIVERY_ITEM_COST_STRING;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.MISSING_IMAGE_DELIVERY_URL;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.REQUEST_ID_VALUE;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.TOTAL_ITEM_COST;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestUtils.verifyCreationTimestampsWithinExecutionInterval;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
class MissingImageDeliveryItemControllerIntegrationTest extends AbstractMongoConfig {

    private static final String TOKEN_PERMISSION_VALUE = "user_orders=%s";
    private static final String MISSING_IMAGE_DELIVERY_ID = "MID-462515-995726";
    private static final String COMPANY_NUMBER = "00006400";
    private static final String COMPANY_NAME = "THE GIRLS' DAY SCHOOL TRUST";
    private static final String CUSTOMER_REFERENCE = "MID Item ordered by Yiannis";
    private static final int QUANTITY_1 = 1;
    private static final String FILING_HISTORY_ID = "MzAwOTM2MDg5OWFkaXF6a2N5";
    private static final String FILING_HISTORY_DATE = "2010-02-12";
    private static final String FILING_HISTORY_DESCRIPTION = "change-person-director-company-with-change-date";
    private static final Map<String, Object> FILING_HISTORY_DESCRIPTION_VALUES;
    private static final Links LINKS;
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String POSTAGE_COST = "0";
    public static final String FILING_HISTORY_TYPE_CH01 = "CH01";
    public static final String FILING_HISTORY_CATEGORY_ACCOUNTS = "accounts";
    public static final String FILING_HISTORY_BARCODE = "0006578";
    private static final String KIND = "item#missing-image-delivery";
    private static final boolean POSTAL_DELIVERY = false;

    private static final boolean USER_NOT_ELIGIBLE_FREE_CERTIFICATES = false;

    private static final ItemCostCalculation CALCULATION = new ItemCostCalculation(
            singletonList(new ItemCosts(DISCOUNT_APPLIED,
                    MISSING_IMAGE_DELIVERY_ITEM_COST_STRING,
                    CALCULATED_COST,
                    MISSING_IMAGE_DELIVERY_ACCOUNTS)),
            POSTAGE_COST,
            TOTAL_ITEM_COST);

    private static final ProductType ACCOUNTS_PRODUCT_TYPE = MISSING_IMAGE_DELIVERY_ACCOUNTS;

    static {
        LINKS = new Links();
        LINKS.setSelf(MISSING_IMAGE_DELIVERY_URL + "/" + MISSING_IMAGE_DELIVERY_ID);
        FILING_HISTORY_DESCRIPTION_VALUES = new HashMap<>();
        FILING_HISTORY_DESCRIPTION_VALUES.put("change_date", "2010-02-12");
        FILING_HISTORY_DESCRIPTION_VALUES.put("officer_name", "Thomas David Wheare");
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MissingImageDeliveryItemRepository repository;
    @MockBean
    private IdGeneratorService idGeneratorService;
    @MockBean
    private CompanyService companyService;
    @MockBean
    private ApiClientService apiClientService;
    @MockBean
    private EtagGeneratorService etagGeneratorService;
    @MockBean
    private MissingImageDeliveryCostCalculatorService calculatorService;
    @MockBean
    private FilingHistoryDocumentService filingHistoryDocumentService;

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Successfully creates missing image delivery item")
    void createMissingImageDeliveryItemSuccessfullyCreatesMissingImageDeliveryItem() throws Exception {
        final MissingImageDeliveryItemRequestDTO missingImageDeliveryItemDTORequest = new MissingImageDeliveryItemRequestDTO();
        final MissingImageDeliveryItemOptionsRequestDto missingImageDeliveryItemOptionsRequestDto
            = new MissingImageDeliveryItemOptionsRequestDto();
        missingImageDeliveryItemOptionsRequestDto.setFilingHistoryId(FILING_HISTORY_ID);

        missingImageDeliveryItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
        missingImageDeliveryItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
        missingImageDeliveryItemDTORequest.setItemOptions(missingImageDeliveryItemOptionsRequestDto);
        missingImageDeliveryItemDTORequest.setQuantity(QUANTITY_1);

        final MissingImageDeliveryItemOptions filing =
            new MissingImageDeliveryItemOptions(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION,
                FILING_HISTORY_DESCRIPTION_VALUES,
                FILING_HISTORY_ID,
                FILING_HISTORY_TYPE_CH01,
                FILING_HISTORY_CATEGORY_ACCOUNTS,
                FILING_HISTORY_BARCODE);

        when(idGeneratorService.autoGenerateId()).thenReturn(MISSING_IMAGE_DELIVERY_ID);
        when(etagGeneratorService.generateEtag()).thenReturn(TOKEN_ETAG);
        when(calculatorService.calculateCosts(QUANTITY_1, ACCOUNTS_PRODUCT_TYPE, USER_NOT_ELIGIBLE_FREE_CERTIFICATES)).thenReturn(CALCULATION);
        when(companyService.getCompanyName(COMPANY_NUMBER)).thenReturn(COMPANY_NAME);
        when(filingHistoryDocumentService.getFilingHistoryDocument(eq(COMPANY_NUMBER), anyString())).thenReturn(filing);

        final MissingImageDeliveryItemResponseDTO expectedItem = new MissingImageDeliveryItemResponseDTO();
        expectedItem.setId(MISSING_IMAGE_DELIVERY_ID);
        expectedItem.setEtag(TOKEN_ETAG);
        expectedItem.setLinks(LINKS);
        expectedItem.setKind(KIND);
        expectedItem.setCompanyNumber(COMPANY_NUMBER);
        expectedItem.setCompanyName(COMPANY_NAME);
        expectedItem.setCustomerReference(CUSTOMER_REFERENCE);
        expectedItem.setQuantity(QUANTITY_1);
        expectedItem.setItemCosts(CALCULATION.getItemCosts());
        expectedItem.setPostageCost(CALCULATION.getPostageCost());
        expectedItem.setTotalItemCost(CALCULATION.getTotalItemCost());
        expectedItem.setPostalDelivery(POSTAL_DELIVERY);
        expectedItem.setItemOptions(filing);

        final LocalDateTime intervalStart = LocalDateTime.now();

        mockMvc.perform(post(MISSING_IMAGE_DELIVERY_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, "create"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingImageDeliveryItemDTORequest))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItem)))
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.company_name", is(COMPANY_NAME)))
                .andExpect(jsonPath("$.kind", is(KIND)))
                .andExpect(jsonPath("$.customer_reference", is(CUSTOMER_REFERENCE)))
                .andExpect(jsonPath("$.item_options.filing_history_date",
                    is(FILING_HISTORY_DATE)))
                .andExpect(jsonPath("$.item_options.filing_history_description",
                    is(FILING_HISTORY_DESCRIPTION)))
                .andExpect(jsonPath("$.item_options.filing_history_description_values",
                    is(FILING_HISTORY_DESCRIPTION_VALUES)))
                .andExpect(jsonPath("$.item_options.filing_history_id",
                    is(FILING_HISTORY_ID)))
                .andExpect(jsonPath("$.item_options.filing_history_type",
                    is(FILING_HISTORY_TYPE_CH01)))
                .andExpect(jsonPath("$.item_options.filing_history_category",
                        is(FILING_HISTORY_CATEGORY_ACCOUNTS)))
                .andExpect((jsonPath("$.item_options.filing_history_barcode",
                        is(FILING_HISTORY_BARCODE))));

        final MissingImageDeliveryItem retrievedItem = assertItemSavedCorrectly(MISSING_IMAGE_DELIVERY_ID);
        final LocalDateTime intervalEnd = LocalDateTime.now();
        verifyCreationTimestampsWithinExecutionInterval(retrievedItem, intervalStart, intervalEnd);
        assertThat(retrievedItem.getEtag(), is(TOKEN_ETAG));
        assertThat(retrievedItem.getLinks(), is(LINKS));
        assertThat(retrievedItem.getCompanyNumber(), is(COMPANY_NUMBER));
        assertThat(retrievedItem.getCompanyName(), is(COMPANY_NAME));
        assertThat(retrievedItem.getCustomerReference(), is(CUSTOMER_REFERENCE));
        assertThat(retrievedItem.getQuantity(), is(QUANTITY_1));

        assertThat(retrievedItem.getItemCosts().size(), is(1));
        assertThat(retrievedItem.getItemCosts().size(), is(expectedItem.getItemCosts().size()));
        org.assertj.core.api.Assertions.assertThat(retrievedItem.getItemCosts().get(0))
                .isEqualToComparingFieldByField(CALCULATION.getItemCosts().get(0));

        assertThat(retrievedItem.getPostageCost(), is(CALCULATION.getPostageCost()));
        assertThat(retrievedItem.getTotalItemCost(), is(CALCULATION.getTotalItemCost()));
        assertThat(retrievedItem.isPostalDelivery(), is(POSTAL_DELIVERY));

        assertThat(retrievedItem.getItemOptions().getFilingHistoryDate(), is (FILING_HISTORY_DATE));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryDescription(), is (FILING_HISTORY_DESCRIPTION));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryDescriptionValues(), is(FILING_HISTORY_DESCRIPTION_VALUES));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryId(), is(FILING_HISTORY_ID));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryType(), is(FILING_HISTORY_TYPE_CH01));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryCategory(), is(FILING_HISTORY_CATEGORY_ACCOUNTS));
        assertThat(retrievedItem.getItemOptions().getFilingHistoryBarcode(), is(FILING_HISTORY_BARCODE));
    }

    @Test
    @DisplayName("Successfully gets a missing image delivery item")
    void getMissingImageDeliveryItemSuccessfully() throws Exception {
        final MissingImageDeliveryItem item = newMissingImageDeliveryItem();
        repository.save(item);

        final MissingImageDeliveryItemResponseDTO expectedItem = new MissingImageDeliveryItemResponseDTO();
        expectedItem.setCompanyNumber(item.getCompanyNumber());
        expectedItem.setCompanyName(item.getCompanyName());
        expectedItem.setQuantity(item.getQuantity());
        expectedItem.setId(item.getId());
        expectedItem.setCustomerReference(item.getCustomerReference());
        expectedItem.setEtag(item.getEtag());
        expectedItem.setLinks(item.getLinks());
        expectedItem.setPostageCost(item.getPostageCost());
        expectedItem.setItemOptions(item.getItemOptions());
        expectedItem.setPostalDelivery(item.isPostalDelivery());
        expectedItem.setKind(item.getKind());

        // When and then
        mockMvc.perform(get(MISSING_IMAGE_DELIVERY_URL + "/" + item.getId())
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, "read"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItem), true))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Returns not found when a missing image delivery item does not exist")
    void getMissingImageDeliveryItemReturnsNotFound() throws Exception {
        // When and then
        mockMvc.perform(get(MISSING_IMAGE_DELIVERY_URL + "/" + MISSING_IMAGE_DELIVERY_ID)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Get missing image delivery returns unauthorised when using the wrong token permissions")
    void getMissingImageDeliveryUnauthorised() throws Exception {
        final MissingImageDeliveryItem item = newMissingImageDeliveryItem();
        repository.save(item);

        // When and then
        mockMvc.perform(get(MISSING_IMAGE_DELIVERY_URL + "/" + item.getId())
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, "update"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Fails to create missing image delivery item that fails validation")
    void createMissingImageDeliveryItemFailsToCreateMissingImageDeliveryItem() throws Exception {
        final MissingImageDeliveryItemOptionsRequestDto missingImageDeliveryItemOptionsRequestDto
                = new MissingImageDeliveryItemOptionsRequestDto();
        final MissingImageDeliveryItemRequestDTO missingImageDeliveryItemDTORequest
                = new MissingImageDeliveryItemRequestDTO();
        missingImageDeliveryItemDTORequest.setItemOptions(missingImageDeliveryItemOptionsRequestDto);


        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST, asList("company_number: must not be null",
                        "item_options.filing_history_id: must not be empty",
                        "quantity: must not be null"));

        when(idGeneratorService.autoGenerateId()).thenReturn(MISSING_IMAGE_DELIVERY_ID);

        mockMvc.perform(post(MISSING_IMAGE_DELIVERY_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, "create"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingImageDeliveryItemDTORequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(expectedValidationError)));

        assertItemWasNotSaved(MISSING_IMAGE_DELIVERY_ID);

    }
    
    @Test
    @DisplayName("Create missing image delivery returns unauthorised when using the wrong token permissions")
    void createMissingImageDeliveryItemUnauthorised() throws Exception {
        final MissingImageDeliveryItemRequestDTO missingImageDeliveryItemDTORequest
                = new MissingImageDeliveryItemRequestDTO();

        mockMvc.perform(post(MISSING_IMAGE_DELIVERY_URL)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
                .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, "read"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingImageDeliveryItemDTORequest)))
                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(idGeneratorService);

    }

    private MissingImageDeliveryItem newMissingImageDeliveryItem() {
        final MissingImageDeliveryItemData itemData = new MissingImageDeliveryItemData();
        itemData.setCompanyName(COMPANY_NAME);
        itemData.setCompanyNumber(COMPANY_NUMBER);
        itemData.setId(MISSING_IMAGE_DELIVERY_ID);
        itemData.setQuantity(QUANTITY_1);
        itemData.setCustomerReference(CUSTOMER_REFERENCE);
        itemData.setEtag(TOKEN_ETAG);
        itemData.setLinks(LINKS);
        itemData.setPostageCost(POSTAGE_COST);
        final MissingImageDeliveryItem item = new MissingImageDeliveryItem();
        item.setData(itemData);
        item.setId(MISSING_IMAGE_DELIVERY_ID);
        item.setCompanyName(COMPANY_NAME);
        item.setCompanyNumber(COMPANY_NUMBER);
        item.setCustomerReference(CUSTOMER_REFERENCE);
        item.setQuantity(QUANTITY_1);
        item.setEtag(TOKEN_ETAG);
        item.setLinks(LINKS);
        item.setKind(KIND);
        item.setPostageCost(POSTAGE_COST);
        item.setUserId(ERIC_IDENTITY_VALUE);
        final MissingImageDeliveryItemOptions itemOptions = new MissingImageDeliveryItemOptions(FILING_HISTORY_DATE,
                FILING_HISTORY_DESCRIPTION, FILING_HISTORY_DESCRIPTION_VALUES, FILING_HISTORY_ID, FILING_HISTORY_TYPE_CH01,
                FILING_HISTORY_CATEGORY_ACCOUNTS, FILING_HISTORY_BARCODE);
        item.setItemOptions(itemOptions);
        return item;
    }

    /**
     * Verifies that the missing image delivery item can be retrieved from the database
     * using its expected ID value.
     *
     * @param missingImageDeliveryId the expected ID of the newly created item
     * @return the retrieved missingImageDelivery item, for possible further verification
     */
    private MissingImageDeliveryItem assertItemSavedCorrectly(final String missingImageDeliveryId) {
        final Optional<MissingImageDeliveryItem> retrievedMissingImageDeliveryItem = repository.findById(missingImageDeliveryId);
        assertThat(retrievedMissingImageDeliveryItem.isPresent(), is(true));
        assertThat(retrievedMissingImageDeliveryItem.get().getId(), is(missingImageDeliveryId));
        return retrievedMissingImageDeliveryItem.get();
    }

    /**
     * Verifies that the missing image delivery item cannot in fact be retrieved
     * from the database.
     *
     * @param missingImageDeliveryId the expected ID of the newly created item
     */
    private void assertItemWasNotSaved(final String missingImageDeliveryId) {
        final Optional<MissingImageDeliveryItem> retrievedMissingImageDeliveryItem
                = repository.findById(missingImageDeliveryId);
        assertThat(retrievedMissingImageDeliveryItem.isPresent(), is(false));
    }

}
