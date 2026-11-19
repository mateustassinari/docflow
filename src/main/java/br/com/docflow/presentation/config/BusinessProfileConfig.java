package br.com.docflow.presentation.config;

import br.com.docflow.domain.model.BusinessProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessProfileConfig {

    @Bean
    public BusinessProfile businessProfile(
            @Value("${app.branding.business-name:DocFlow}") String businessName,
            @Value("${app.branding.owner-name:}") String ownerName,
            @Value("${app.branding.signature-image:}") String signatureImage
    ) {
        return new BusinessProfile(businessName, ownerName, signatureImage);
    }
}
