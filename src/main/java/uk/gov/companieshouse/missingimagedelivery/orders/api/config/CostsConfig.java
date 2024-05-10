package uk.gov.companieshouse.missingimagedelivery.orders.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

@Configuration
@Component
@PropertySource(value = "classpath:costs.yaml", factory = YamlPropertyLoaderFactory.class)
@ConfigurationProperties(prefix = "costs")
@Validated
public class CostsConfig {

    @Min(1)
    private int missingImageDeliveryItemCost;

    public int getMissingImageDeliveryItemCost() {
        return missingImageDeliveryItemCost;
    }

    public void setMissingImageDeliveryItemCost(int missingImageDeliveryItemCost) {
        this.missingImageDeliveryItemCost = missingImageDeliveryItemCost;
    }

}
