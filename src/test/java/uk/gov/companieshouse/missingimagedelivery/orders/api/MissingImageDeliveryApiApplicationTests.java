package uk.gov.companieshouse.missingimagedelivery.orders.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = { "spring.config.location=classpath:application.properties" })
class MissingImageDeliveryApiApplicationTests {

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void contextLoads() {
    }
}
