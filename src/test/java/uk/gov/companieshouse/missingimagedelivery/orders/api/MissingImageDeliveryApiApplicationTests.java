package uk.gov.companieshouse.missingimagedelivery.orders.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class MissingImageDeliverypiApplicationTests {

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void contextLoads() {
    }
}
