package uk.gov.companieshouse.missingimagedelivery.orders.api.service;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.missingimagedelivery.orders.api.logging.LoggingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Service
public class DescriptionProviderService {
    private static final Logger LOGGER = LoggingUtils.getLogger();

    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY = "missing-image-delivery-description";
    private static final String COMPANY_MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY = "company-missing-image-delivery";

    private static final String ORDERS_DESCRIPTIONS_FILEPATH = "api-enumerations/orders_descriptions.yaml";

    private final String missingImageDeliveryDescription;

    private static final String LOG_MESSAGE_FILE_KEY = "file";

    public DescriptionProviderService() {
        final File ordersDescriptionsFile = new File(ORDERS_DESCRIPTIONS_FILEPATH);
        missingImageDeliveryDescription = getMissingImageDeliveryDescription(ordersDescriptionsFile);
    }

    public DescriptionProviderService(final File ordersDescriptionsFile) {
        missingImageDeliveryDescription = getMissingImageDeliveryDescription(ordersDescriptionsFile);
    }

    /**
     * Gets the configured description.
     * @param companyNumber the company number making up part of the description
     * @return the configured description, or <code>null</code> if none found.
     */
    public String getDescription(final String companyNumber) {
        if (missingImageDeliveryDescription == null) {
            LoggingUtils.logOrdersDescriptionsConfigError(COMPANY_MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY,
                    "Missing image delivery description not found in orders descriptions file");
            return null;
        }
        final Map<String, String> descriptionValues = singletonMap(COMPANY_NUMBER_KEY, companyNumber);
        return StrSubstitutor.replace(missingImageDeliveryDescription, descriptionValues, "{", "}");
    }

    /**
     * Looks up the missing image delivery description by its key 'company-missing-image-delivery' under the
     * 'missing-image-delivery-description' section of the orders descriptions YAML file.
     * @param ordersDescriptionsFile the orders descriptions YAML file
     * @return the value found or <code>null</code> if none found.
     */
    private String getMissingImageDeliveryDescription(final File ordersDescriptionsFile) {

        if (!ordersDescriptionsFile.exists()) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_FILE_KEY, ordersDescriptionsFile.getAbsolutePath());
            LoggingUtils.getLogger().error("Orders descriptions file not found", logMap);
            return null;
        }

        String missingImageDeliveryDesc = null;
        try(final InputStream inputStream = new FileInputStream(ordersDescriptionsFile)) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> orderDescriptions = yaml.load(inputStream);
            final Map<String, String> missingImageDeliveryDescriptions =
                    (Map<String, String>) orderDescriptions.get(MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY);
            if (missingImageDeliveryDescriptions == null) {
                LoggingUtils.logOrdersDescriptionsConfigError(MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY,
                        "Missing image delivery descriptions not found in orders descriptions file");
                return null;
            }

            missingImageDeliveryDesc = missingImageDeliveryDescriptions.get(COMPANY_MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY);
            if (missingImageDeliveryDesc == null) {
                LoggingUtils.logOrdersDescriptionsConfigError(COMPANY_MISSING_IMAGE_DELIVERY_DESCRIPTION_KEY,
                        "Missing image delivery description not found in orders descriptions file");
            }
        } catch (IOException ioe) {
            // This is very unlikely to happen here given File.exists() check above,
            // and that it is not likely to encounter an error closing the stream either.
            LOGGER.error("Error reading orders_descriptions.yaml file", ioe);
        }
        return missingImageDeliveryDesc;
    }

}
