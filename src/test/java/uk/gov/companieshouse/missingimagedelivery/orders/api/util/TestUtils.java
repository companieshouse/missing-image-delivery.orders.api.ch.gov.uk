package uk.gov.companieshouse.missingimagedelivery.orders.api.util;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.springframework.core.env.Environment;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestUtils {

    /**
     * Configures the CH Java SDKs used in WireMock based integration tests to interact with with WireMock
     * stubbed/mocked API endpoints.
     * @param environment the Spring {@link Environment} the tests are run in
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @return the WireMock port value used for the tests
     */
    public static String givenSdkIsConfigured(final Environment environment, final EnvironmentVariables variables) {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://localhost:" + wireMockPort);
        variables.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        return wireMockPort;
    }
    /**
     * Verifies that the item created at and updated at timestamps are within the expected interval
     * for item creation.
     * @param itemCreated the item created
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    public static void verifyCreationTimestampsWithinExecutionInterval(final MissingImageDeliveryItem itemCreated,
                                                                       final LocalDateTime intervalStart,
                                                                       final LocalDateTime intervalEnd) {
        assertThat(itemCreated.getCreatedAt().isAfter(intervalStart) ||
                itemCreated.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getCreatedAt().isBefore(intervalEnd) ||
                itemCreated.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(itemCreated.getUpdatedAt().isAfter(intervalStart) ||
                itemCreated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getUpdatedAt().isBefore(intervalEnd) ||
                itemCreated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }
}
