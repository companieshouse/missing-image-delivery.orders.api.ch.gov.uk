package uk.gov.companieshouse.missingimagedelivery.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class HealthcheckControllerTest {

    @InjectMocks
    private HealthcheckController controllerUnderTest;

    @Test
    @DisplayName("Health check confirms health with HTTP 200")
    public void applicationHealthcheckRunsSuccessfully(){
        // When the health endpoint is polled
        final ResponseEntity<Void> response = controllerUnderTest.getHealthCheck();

        // Then the response is HTTP 200
        assertEquals( HttpStatus.OK, response.getStatusCode());
    }
}
