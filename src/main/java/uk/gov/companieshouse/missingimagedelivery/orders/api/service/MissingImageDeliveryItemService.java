package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.FilingHistoryCategory;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItem;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.MissingImageDeliveryItemOptions;
import uk.gov.companieshouse.missingimagedelivery.orders.api.model.ProductType;
import uk.gov.companieshouse.missingimagedelivery.orders.api.repository.MissingImageDeliveryItemRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for the management and storage of missing image delivery items.
 */
@Service
public class MissingImageDeliveryItemService {

    private final MissingImageDeliveryItemRepository repository;
    private final IdGeneratorService idGenerator;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;
    private final MissingImageDeliveryCostCalculatorService calculator;
    private final DescriptionProviderService descriptionProviderService;
    private static final String DESCRIPTION_IDENTIFIER = "missing-image-delivery";
    private static final String COMPANY_NUMBER_KEY = "company_number";


    private static final String KIND = "item#missing-image-delivery";

    public MissingImageDeliveryItemService(final MissingImageDeliveryItemRepository repository,
                                     final IdGeneratorService idGenerator,
                                     final EtagGeneratorService etagGenerator,
                                     final LinksGeneratorService linksGenerator,
                                     final MissingImageDeliveryCostCalculatorService calculator,
                                     final DescriptionProviderService descriptionProvider) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
        this.calculator = calculator;
        this.descriptionProviderService = descriptionProvider;
    }

    /**
     * Creates the missing image delivery item in the database.
     *
     * @param item the item to be created
     * @return the created item
     */
    public MissingImageDeliveryItem createMissingImageDeliveryItem(final MissingImageDeliveryItem item, final boolean userGetsFreeCertificates) {
        item.setId(idGenerator.autoGenerateId());
        setCreationDateTimes(item);
        item.setEtag(etagGenerator.generateEtag());
        item.setLinks(linksGenerator.generateLinks(item.getId()));
        item.setKind(KIND);

        populateDescriptions(item);

        MissingImageDeliveryItemOptions itemOptions = item.getItemOptions();
        String category = itemOptions.getFilingHistoryCategory();
        ProductType productType = FilingHistoryCategory.enumValueOf(category).getProductType();
        final ItemCostCalculation costs = calculator.calculateCosts(item.getQuantity(), productType, userGetsFreeCertificates );
        item.setItemCosts(costs.getItemCosts());
        item.setPostageCost(costs.getPostageCost());
        item.setTotalItemCost(costs.getTotalItemCost());
        return repository.save(item);
    }

    public void populateItemCosts(final MissingImageDeliveryItem item,
                                  final boolean userGetsFreeCertificates) {
        MissingImageDeliveryItemOptions itemOptions = item.getItemOptions();
        String category = itemOptions.getFilingHistoryCategory();
        ProductType productType = FilingHistoryCategory.enumValueOf(category).getProductType();
        final ItemCostCalculation costs = calculator.calculateCosts(item.getQuantity(), productType, userGetsFreeCertificates );
        item.setItemCosts(costs.getItemCosts());
        item.setPostageCost(costs.getPostageCost());
        item.setTotalItemCost(costs.getTotalItemCost());
    }

    private void populateDescriptions(final MissingImageDeliveryItem missingImageDeliveryItem) {
        String description = descriptionProviderService.getDescription(missingImageDeliveryItem.getData().getCompanyNumber());
        missingImageDeliveryItem.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        missingImageDeliveryItem.setDescription(description);

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put(DESCRIPTION_IDENTIFIER, description);
        descriptionValues.put(COMPANY_NUMBER_KEY, missingImageDeliveryItem.getData().getCompanyNumber());
        missingImageDeliveryItem.setDescriptionValues(descriptionValues);
    }

    /**
     * Sets the created at and updated at date time 'timestamps' to now.
     *
     * @param item the item to be 'timestamped'
     */
    void setCreationDateTimes(final MissingImageDeliveryItem item) {
        final LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
    }

    /**
     * Returns missing image delivery item
     * @param id id of the requested item
     * @return
     */
    public Optional<MissingImageDeliveryItem> getMissingImageDeliveryItemById(String id) {
        return repository.findById(id);
    }

    public Optional<MissingImageDeliveryItem> getMissingImageDeliveryItemWithCosts(String id, final boolean userGetsFreeCertificates) {
        Optional<MissingImageDeliveryItem>  retrievedItem = getMissingImageDeliveryItemById(id);

        retrievedItem.ifPresent(item -> populateItemCosts(item, userGetsFreeCertificates));

        return retrievedItem;
    }
}
