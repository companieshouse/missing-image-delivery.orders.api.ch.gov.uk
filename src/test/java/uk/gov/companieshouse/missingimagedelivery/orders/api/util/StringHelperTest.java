package uk.gov.companieshouse.missingimagedelivery.orders.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

class StringHelperTest {

    private static final String FREE_CERTS= "/admin/free-certs";
    private static final String FREE_MIDS= "/admin/free-mids";
    private static final String FREE_CERT_DOCS= "/admin/free-cert-docs";
    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = null;


    private final StringHelper stringHelper = new StringHelper();


    static Stream<Arguments> provideParamArgsForAsSetTests() {
        return Stream.of(
                // Checking comma as a delimeter
                Arguments.of(FREE_CERTS + "," + FREE_MIDS + "," + FREE_CERT_DOCS, ",", Set.of(FREE_CERTS, FREE_MIDS, FREE_CERT_DOCS)),
                Arguments.of(EMPTY_STRING, ",", Set.of()),
                Arguments.of(NULL_STRING, ",", Set.of()),
                // Checking parsing works with whitespace as delimeter
                Arguments.of(" " + FREE_CERTS + " " + FREE_MIDS + " " + FREE_CERT_DOCS + " ", "\\s+", Set.of(FREE_CERTS, FREE_MIDS, FREE_CERT_DOCS))
        );
    }

    @ParameterizedTest
    @MethodSource("provideParamArgsForAsSetTests")
    @DisplayName("Should return a set with values split by a specified delimiter and handles null/empty values ")
    void asSetSplitsAndHandlesEmptyValues(String input, String delimiter, Set<String> expectedSet) {
        Set<String> result = stringHelper.asSet(delimiter, input);
        Assertions.assertEquals(expectedSet, result);
    }
}
