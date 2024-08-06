package uk.gov.companieshouse.missingimagedelivery.orders.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemResponseDTO;
import uk.gov.companieshouse.missingimagedelivery.orders.api.interceptor.EricAuthoriser;
import uk.gov.companieshouse.missingimagedelivery.orders.api.mapper.MissingImageDeliveryItemMapper;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemData;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.MissingImageDeliveryItemService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.util.TestConstants.REQUEST_ID_VALUE;

@ExtendWith(MockitoExtension.class)
public class MissingImageDeliveryItemControllerTest {

    private static final String ID = "MID-552015-995597";

    @InjectMocks
    private MissingImageDeliveryItemController controllerUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private EricAuthoriser authoriser;

    @Mock
    private MissingImageDeliveryItemService missingImageDeliveryItemService;

    @Mock
    private MissingImageDeliveryItem item;

    @Mock
    private MissingImageDeliveryItemData data;

    @Mock
    private MissingImageDeliveryItemResponseDTO dto;

    @Mock
    private MissingImageDeliveryItemMapper mapper;

    @Test
    @DisplayName("GET missing image delivery resource returns item")
    void getMissingImageDeliveryItemPresent() {
        when(missingImageDeliveryItemService.getMissingImageDeliveryItemWithCosts(ID, false)).thenReturn(Optional.of(item));
        when(item.getData()).thenReturn(data);
        when(mapper.missingImageDeliveryItemToMissingImageDeliveryItemResponseDTO(data)).thenReturn(dto);
        ResponseEntity<Object> response = controllerUnderTest.getMissingImageDeliveryItem(ID, REQUEST_ID_VALUE, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    @DisplayName("Get missing image delivery resource returns HTTP NOT FOUND")
    void getMissingImageDeliveryItemNotFound() {
        when(missingImageDeliveryItemService.getMissingImageDeliveryItemWithCosts(ID, false)).thenReturn(Optional.empty());
        ResponseEntity<Object> response = controllerUnderTest.getMissingImageDeliveryItem(ID, REQUEST_ID_VALUE, request);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}