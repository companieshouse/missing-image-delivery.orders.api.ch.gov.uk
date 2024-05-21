package uk.gov.companieshouse.missingimagedelivery.orders.api.config;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.missingimagedelivery.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.missingimagedelivery.orders.api.interceptor.UserAuthorisationInterceptor;

@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Value("${uk.gov.companieshouse.missingimagedelivery.orders.api.home}")
    String missingImageDeliveryHome;

    private final UserAuthenticationInterceptor userAuthenticationInterceptor;

    private final UserAuthorisationInterceptor userAuthorisationInterceptor;

    private final CRUDAuthenticationInterceptor crudPermissionsInterceptor;

    public ApplicationConfiguration(
            UserAuthenticationInterceptor userAuthenticationInterceptor,
            UserAuthorisationInterceptor userAuthorisationInterceptor,
            @Lazy CRUDAuthenticationInterceptor crudPermissionsInterceptor) {
        this.userAuthorisationInterceptor = userAuthorisationInterceptor;
        this.userAuthenticationInterceptor = userAuthenticationInterceptor;
        this.crudPermissionsInterceptor = crudPermissionsInterceptor;
    }


    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final String authPathPattern = missingImageDeliveryHome + "/**";
        final String healthCheckPathPattern = missingImageDeliveryHome + "/healthcheck";
        registry.addInterceptor(userAuthenticationInterceptor).addPathPatterns(authPathPattern)
                .excludePathPatterns(healthCheckPathPattern);
        registry.addInterceptor(userAuthorisationInterceptor).addPathPatterns(authPathPattern);
        registry.addInterceptor(crudPermissionsInterceptor).addPathPatterns(authPathPattern)
                .excludePathPatterns(healthCheckPathPattern);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(SNAKE_CASE)
                .findAndRegisterModules();
    }

    @Bean
    public CRUDAuthenticationInterceptor crudPermissionsInterceptor() {
        return new CRUDAuthenticationInterceptor(Key.USER_ORDERS);
    }
}
