package uk.gov.companieshouse.missingimagedelivery.orders.api.controller;

import org.springframework.http.HttpStatusCode;

import java.util.List;

/**
 * Wraps up status and list of messages for rendering in non-2xx REST response payload.
 */
public class ApiError {

    private final HttpStatusCode statusCode;
    private final List<String> errors;

    public ApiError(final HttpStatusCode statusCode, final List<String> errors) {
        super();
        this.statusCode = statusCode;
        this.errors = errors;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public List<String> getErrors() {
        return errors;
    }


}
