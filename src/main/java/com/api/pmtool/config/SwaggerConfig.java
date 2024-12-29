package com.api.pmtool.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Return the OpenAPI configuration for the application.
     * This defines the structure of the Swagger documentation for the API.
     * The API is documented with the title "Demand Management API", a description of
     * "API documentation for Demand Management System", version "v1.0", and licensed under
     * the Apache 2.0 license. The external documentation points to the Spring Boot
     * documentation.
     * 
     * @return The OpenAPI configuration for the application.
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
/*         .components(new Components()
                        .addSchemas("MultipartFile", new Schema().type("string").format("binary"))) */
                .info(new Info().title("Demand Management API")
                        .description("API documentation for Demand Management System")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Spring Boot Documentation")
                        .url("https://spring.io/projects/spring-boot"));
                
    }
}
