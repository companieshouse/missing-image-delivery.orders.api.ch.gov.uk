package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.api.error.ApiErrorResponseException.fromIOException;

import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filinghistory.FilingResourceHandler;
import uk.gov.companieshouse.api.handler.filinghistory.request.FilingGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;

import java.io.IOException;

/**
 * Unit tests the {@link FilingHistoryDocumentService} class.
 */
@ExtendWith(MockitoExtension.class)
@PrepareForTest(HttpResponseException.class)
public class FilingHistoryDocumentServiceTest {

    private static final String COMPANY_NUMBER = "00006400";

    private static final String INVALID_URI = "URI pattern does not match expected URI pattern for this resource.";
    private static final String INVALID_URI_EXPECTED_REASON =
            "Invalid URI /company/00006400/filing-history/1 for filing";

    private static final String IOEXCEPTION_MESSAGE = "IOException thrown by test";
    private static final String IOEXCEPTION_EXPECTED_REASON =
            "Error sending request to http://host/company/00006400/filing-history/1: " + IOEXCEPTION_MESSAGE;

    private static final String NOT_FOUND_EXPECTED_REASON = "Error getting filing history document 1 for company number "
            + COMPANY_NUMBER + ".";

    private static final String FILING_SOUGHT = "1";

    @InjectMocks
    private FilingHistoryDocumentService serviceUnderTest;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private FilingResourceHandler resourceHandler;

    @Mock
    private FilingGet filingGet;

    @Mock
    private ApiResponse<FilingApi> response;

    @Mock
    private FilingApi filing;


    @Test
    @DisplayName("getFilingHistoryDocuments() reports a URIValidationException as an Internal Server Error (500)")
    void uriValidationExceptionReportedAsServerInternalError() throws Exception  {

        // Given
        setUpForFilingApiException(new URIValidationException(INVALID_URI));

        // When and then
        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocument(COMPANY_NUMBER, FILING_SOUGHT));
        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), is(INVALID_URI_EXPECTED_REASON));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments() reports an IOException as an Internal Server Error (500)")
    void serverInternalErrorReportedAsSuch() throws Exception {

        // Given
        setUpForFilingApiException(fromIOException(new IOException(IOEXCEPTION_MESSAGE)));
        when(internalApiClient.getBasePath()).thenReturn("http://host");

        // When and then
        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocument(COMPANY_NUMBER, FILING_SOUGHT));
        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), is(IOEXCEPTION_EXPECTED_REASON));
    }

    /**
     * Provides set up for testing what happens when the Filing API throws an exception during the execution of
     * {@link FilingHistoryDocumentService#getFilingHistoryDocument(String, String)}.
     * @param exceptionToThrow the exception to throw
     * @throws ApiErrorResponseException should something unexpected happen
     * @throws URIValidationException should something unexpected happen
     */
    private void setUpForFilingApiException(final Exception exceptionToThrow)
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.filing()).thenReturn(resourceHandler);
        when(resourceHandler.get("/company/00006400/filing-history/1")).thenReturn(filingGet);
        when(filingGet.execute()).thenThrow(exceptionToThrow);
    }

    /**
     * Provides fair weather set up for testing
     * {@link FilingHistoryDocumentService#getFilingHistoryDocument(String, String)}.
     * @throws ApiErrorResponseException should something unexpected happen
     * @throws URIValidationException should something unexpected happen
     */
    private void fairWeatherSetUp() throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.filing()).thenReturn(resourceHandler);
        when(resourceHandler.get("/company/00006400/filing-history/1")).thenReturn(filingGet);
        when(filingGet.execute()).thenReturn(response);
        when(response.getData()).thenReturn(filing);
    }

}
