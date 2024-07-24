package uk.gov.companieshouse.missingimagedelivery.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemRequestDTO;
import uk.gov.companieshouse.missingimagedelivery.orders.api.dto.MissingImageDeliveryItemResponseDTO;
import uk.gov.companieshouse.missingimagedelivery.orders.api.interceptor.EricAuthoriser;
import uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.missingimagedelivery.orders.api.mapper.MissingImageDeliveryItemMapper;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemOptions;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.CompanyService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.service.MissingImageDeliveryItemService;
import uk.gov.companieshouse.missingimagedelivery.orders.api.util.EricHeaderHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.COMPANY_NUMBER_LOG_KEY;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.MISSING_IMAGE_DELIVERY_ID_LOG_KEY;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.STATUS_LOG_KEY;
import static uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils.USER_ID_LOG_KEY;


@RestController
public class MissingImageDeliveryItemController {

    private static final Logger LOGGER = LoggingUtils.getLogger();

    private final MissingImageDeliveryItemMapper mapper;
    private final CompanyService companyService;
    private final MissingImageDeliveryItemService missingImageDeliveryItemService;
    private final FilingHistoryDocumentService filingHistoryDocumentService;
    private EricAuthoriser ericAuthoriser;

    public MissingImageDeliveryItemController(final MissingImageDeliveryItemMapper mapper,
                                        final CompanyService companyService,
                                        final MissingImageDeliveryItemService missingImageDeliveryItemService,
                                        final FilingHistoryDocumentService filingHistoryDocumentService,
                                              final EricAuthoriser ericAuthoriser) {
        this.mapper = mapper;
        this.companyService = companyService;
        this.missingImageDeliveryItemService = missingImageDeliveryItemService;
        this.filingHistoryDocumentService = filingHistoryDocumentService;
        this.ericAuthoriser = ericAuthoriser;
    }

    @PostMapping("${uk.gov.companieshouse.missingimagedelivery.orders.api.home}")
    public ResponseEntity<Object> createMissingImageDeliveryItem(
        final @Valid @RequestBody MissingImageDeliveryItemRequestDTO missingImageDeliveryItemRequestDTO,
        HttpServletRequest request,
        final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        Map<String, Object> logMap = LoggingUtils.createLoggingDataMap(requestId);
        LoggingUtils.getLogger().infoRequest(request, "create missing image delivery item request", logMap);
        final boolean entitledToFreeCertificates = ericAuthoriser.hasPermission("/admin/free-certs",  request);
        LOGGER.info(String.valueOf(entitledToFreeCertificates));
        MissingImageDeliveryItem item = mapper.missingImageDeliveryItemRequestDTOtoMissingImageDeliveryItem(missingImageDeliveryItemRequestDTO);

        item.setUserId(EricHeaderHelper.getIdentity(request));
        final String companyName = companyService.getCompanyName(item.getCompanyNumber());
        item.setCompanyName(companyName);

        final MissingImageDeliveryItemOptions filing =
            filingHistoryDocumentService.getFilingHistoryDocument(
                item.getData().getCompanyNumber(),
                item.getData().getItemOptions().getFilingHistoryId());

        item.getData().setItemOptions(filing);

        MissingImageDeliveryItem createdItem = missingImageDeliveryItemService.createMissingImageDeliveryItem(item, entitledToFreeCertificates);

        logMap.put(USER_ID_LOG_KEY, createdItem.getUserId());
        logMap.put(COMPANY_NUMBER_LOG_KEY, createdItem.getCompanyNumber());
        logMap.put(MISSING_IMAGE_DELIVERY_ID_LOG_KEY, createdItem.getId());
        logMap.put(STATUS_LOG_KEY, CREATED);
        LoggingUtils.getLogger().infoRequest(request, "missing image delivery item created", logMap);

        final MissingImageDeliveryItemResponseDTO createdMissingImageDeliveryItemResponseDTO = mapper
            .missingImageDeliveryItemToMissingImageDeliveryItemResponseDTO(createdItem.getData());

        return ResponseEntity.status(CREATED).body(createdMissingImageDeliveryItemResponseDTO);
    }

    @GetMapping("${uk.gov.companieshouse.missingimagedelivery.orders.api.home}/{id}")
    public ResponseEntity<Object> getMissingImageDeliveryItem(final @PathVariable String id,
                                                        final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        final Map<String, Object> logMap = LoggingUtils.createLoggingDataMap(requestId);
        logMap.put(MISSING_IMAGE_DELIVERY_ID_LOG_KEY, id);
        LoggingUtils.getLogger().info("get missing image delivery item request", logMap);
        final Optional<MissingImageDeliveryItem> item = missingImageDeliveryItemService.getMissingImageDeliveryItemById(id);
        if (item.isPresent()) {
            final MissingImageDeliveryItemResponseDTO retrievedMissingImageDeliveryItemResponseDTO
                    = mapper.missingImageDeliveryItemToMissingImageDeliveryItemResponseDTO(item.get().getData());
            logMap.put(STATUS_LOG_KEY, OK);
            LoggingUtils.getLogger().info("missing image delivery item found", logMap);
            return ResponseEntity.status(OK).body(retrievedMissingImageDeliveryItemResponseDTO);
        }
        else {
            final String errorMsg = "missing image delivery resource not found";
            final List<String> errors = new ArrayList<>();
            errors.add(errorMsg);
            LoggingUtils.logErrorsWithStatus(logMap, errors, NOT_FOUND);
            LoggingUtils.getLogger().error(errorMsg, logMap);
            return ResponseEntity.status(NOT_FOUND).body(new ApiError(NOT_FOUND, errors));
        }
    }
}
