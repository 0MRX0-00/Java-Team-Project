package com.disasterrelief.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)")
                        .description("REST API specifications for emergency disaster registration, intelligent dispatch scheduling, " +
                                "transport scoring, multithreaded mission execution, delivery audits, and analytical reporting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Emergency Logistics Command Team")
                                .email("logistics@disaster-relief.gov.in"))
                        .license(new License().name("Apache 2.0")));
    }
}

