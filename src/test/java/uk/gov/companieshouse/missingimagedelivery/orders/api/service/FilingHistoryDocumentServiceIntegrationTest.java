package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.FILING_NOT_FOUND;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestUtils.givenSdkIsConfigured;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.http.Fault;
import java.time.LocalDate;
import java.util.Collections;
import org.hamcrest.core.Is;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemOptions;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;


/**
 * Integration tests the {@link FilingHistoryDocumentService}.
 */
@SpringBootTest
@ExtendWith(SystemStubsExtension.class)
@AutoConfigureWireMock(port = 0)
class FilingHistoryDocumentServiceIntegrationTest {

    private static final String COMPANY_NUMBER = "00006400";
    private static final String UNKNOWN_COMPANY_NUMBER = "00000000";
    private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
    private static final String UNKNOWN_ID = "000000000000000000000000";

    private static final MissingImageDeliveryItemOptions FILING_1 = new MissingImageDeliveryItemOptions(
            "2005-03-21",
            "accounts-with-accounts-type-group",
            Collections.singletonMap("made_up_date", "2004-08-31"),
            ID_1,
            "AA",
            "accounts",
            "00503271"
    );

    private static final MissingImageDeliveryItemOptions FILING_SOUGHT =
        new MissingImageDeliveryItemOptions(null,
                null,
                null,
                ID_1,
                null,
                null,
                null
            );

    private static final MissingImageDeliveryItemOptions FILING_EXPECTED = FILING_1;

    @Configuration
    @ComponentScan(basePackageClasses = FilingHistoryDocumentServiceIntegrationTest.class)
    static class Config {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setPropertyNamingStrategy(SNAKE_CASE)
                    .findAndRegisterModules();
        }
    }

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @MockBean
    private MissingImageDeliveryItemService missingImageDeliveryItemService;

    @MockBean
    private MissingImageDeliveryCostCalculatorService missingImageDeliveryCostCalculatorService;

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @BeforeEach
    public void beforeEach(){
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        environmentVariables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        environmentVariables.set("API_URL", "http://localhost:" + wireMockPort);
        environmentVariables.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        environmentVariables.set("DOCUMENT_API_LOCAL_URL", "http://localhost:" + wireMockPort);
    }
    
    

    @Test
    @DisplayName("getFilingHistoryDocument gets the expected filing history document successfully")
    void getFilingHistoryDocumentsSuccessfully() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(filingApi(FILING_1)))));


        // When
        final MissingImageDeliveryItemOptions filing =
                serviceUnderTest.getFilingHistoryDocument(COMPANY_NUMBER, ID_1);


        // Then
        assertThat(filing, is(notNullValue()));
        assertThat(filing.getFilingHistoryId(), is(FILING_SOUGHT.getFilingHistoryId()));
        assertThat(isSemanticallyEquivalent(filing, FILING_EXPECTED), is(true));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 400 Bad Request for an unknown company")
    void getFilingHistoryThrowsBadRequestForUnknownCompany() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + UNKNOWN_COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocument(UNKNOWN_COMPANY_NUMBER, ID_1));
        assertThat(exception.getStatusCode(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + ID_1 +
                " for company number " + UNKNOWN_COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 400 Bad Request for an unknown filing history document")
    void getFilingHistoryThrowsBadRequestForUnknownFilingHistoryDocument() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + UNKNOWN_ID))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocument(COMPANY_NUMBER, UNKNOWN_ID));
        assertThat(exception.getStatusCode(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + UNKNOWN_ID +
                " for company number " + COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 500 Internal Server Error for connection failure")
    void getFilingHistoryThrowsInternalServerErrorForForConnectionFailure() {

        // Given
        final String wireMockPort = givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocument(COMPANY_NUMBER, ID_1));
        assertThat(exception.getStatusCode(), Is.is(INTERNAL_SERVER_ERROR));
        final String expectedReason = "Error sending request to http://localhost:"
                + wireMockPort + "/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1 + ": Connection reset";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }


    /**
     * Factory method that creates an instance of {@link FilingApi} for testing purposes, "reverse-engineered"
     * from the {@link MissingImageDeliveryItemOptions} provided.
     * @param document the filing history document that should result from the filing this creates
     * @return the filing created
     */
    private static FilingApi filingApi(final MissingImageDeliveryItemOptions document) {
        final FilingApi filing = new FilingApi();
        filing.setTransactionId(document.getFilingHistoryId());
        filing.setDate(LocalDate.parse(document.getFilingHistoryDate()));
        filing.setDescriptionValues(document.getFilingHistoryDescriptionValues());
        filing.setDescription(document.getFilingHistoryDescription());
        filing.setType(document.getFilingHistoryType());
        filing.setCategory(document.getFilingHistoryCategory());
        filing.setBarcode(document.getFilingHistoryBarcode());
        return filing;
    }

    /**
     * Serialises the objects to be compared to JSON so that we can compare them without worrying about differences
     * in the type of maps, arrays, etc., underlying their implementations.
     * @param object1 the first object to be compared
     * @param object2 the second object to be compared
     * @return whether the two objects are semantically equivalent (<code>true</code>), or differ (<code>false</code>)
     * @throws JsonProcessingException should something unexpected happen
     */
    private boolean isSemanticallyEquivalent(final Object object1, final Object object2)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(object1).equals(objectMapper.writeValueAsString(object2));
    }

}


