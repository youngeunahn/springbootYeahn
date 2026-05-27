package com.yeahn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI yeahnOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Yeahn API")
                .version("v1")
                .description("Public user APIs for Yeahn."));
    }
}
