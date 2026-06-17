package com.entropyatlas.entropyatlas.config;

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
                        .title("Entropy Atlas API")
                        .version("1.0")
                        .description("Behavioral Stability Intelligence Platform API documentation")
                        .contact(new Contact()
                                .name("Entropy Atlas Team")
                                .email("support@entropyatlas.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
