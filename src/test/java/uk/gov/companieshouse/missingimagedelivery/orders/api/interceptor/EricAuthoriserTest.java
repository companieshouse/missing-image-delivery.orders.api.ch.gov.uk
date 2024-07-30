package uk.gov.companieshouse.missingimagedelivery.orders.api.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.ERIC_AUTHORISED_ROLES;


import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.missingimagedelivery.orders.api.util.StringHelper;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EricAuthoriserTest {

    private static final String FREE_MIDS_PERMISSION= "/admin/free-mids";

    private static final String FREE_CERT_DOCS_HEADER_VALUE= "/admin/free-cert-docs";

    private static final String FREE_MIDS_HEADER_VALUE= "/admin/free-mids";

    @Mock
    private StringHelper stringHelper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EricAuthoriser authoriser;

    @Test
    @DisplayName("Should return true when permission is present in the header")
    void shouldReturnTrueForValidPermission() {
        final String headerValue = FREE_MIDS_HEADER_VALUE;
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(headerValue);
        when(stringHelper.asSet("\\s+", headerValue)).thenReturn(Set.of("/admin/free-mids"));
        boolean hasPermission = authoriser.hasPermission(FREE_MIDS_PERMISSION, request);

        assertTrue(hasPermission);

        verify(request).getHeader(ERIC_AUTHORISED_ROLES);
        verify(stringHelper).asSet("\\s+", headerValue);
    }

    @Test
    @DisplayName("Should return false when free permission is not present in the header")
    void shouldReturnFalseForInvalidPermission() {
        final String headerValue = "non-admin";
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(headerValue);
        when(stringHelper.asSet("\\s+", headerValue)).thenReturn(Set.of("non-admin"));
        boolean hasPermission = authoriser.hasPermission(FREE_MIDS_PERMISSION, request);

        assertFalse(hasPermission);
    }

    @Test
    @DisplayName("Should return false when the ERIC header is null")
    void shouldReturnFalseWhenHeaderIsMissing() {
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(null);
        boolean hasPermission = authoriser.hasPermission(FREE_MIDS_PERMISSION, request);

        assertFalse(hasPermission);
    }

    @Test
    @DisplayName("Should return True when there are multiple permissions in header including free MIDS ")
    void shouldReturnTrueForMultipleWithValidMIDSPermission() {
        String headerValue = FREE_MIDS_HEADER_VALUE + " " + FREE_CERT_DOCS_HEADER_VALUE;
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(headerValue);
        when(stringHelper.asSet("\\s+", headerValue)).thenReturn(Set.of(FREE_MIDS_HEADER_VALUE,FREE_CERT_DOCS_HEADER_VALUE));
        boolean hasPermission = authoriser.hasPermission(FREE_MIDS_PERMISSION, request);

        assertTrue(hasPermission);
    }
}
