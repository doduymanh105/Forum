package com.example.forum.core.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Forum API Document",
                version = "1.0",
                description = "API document for Forum project"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT token. Format: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenApi(){
                final String securitySchemeName = "bearerAuth";
                return new OpenAPI()
                        .info(
                                new io.swagger.v3.oas.models.info.Info()
                                        .title("Forum API Document")
                                        .version("1.0")
                                        .description("API document for Forum project")
                        )
                        .components(new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                        );
        }


        @Bean
        public GlobalOpenApiCustomizer globalOpenApiCustomizer(){

                List<String> publicPaths = Arrays.asList(
                                "login",
                                "register",
                                "/auth"
                        );

                return openApi -> {
                        final String securitySchemeName = "bearerAuth";
                        openApi.getPaths().forEach((path, pathItem)-> {
                                pathItem.readOperations().forEach(
                                        operation -> {
                                                boolean isPublic = publicPaths.stream()
                                                        .anyMatch(keyword -> path.toLowerCase().contains(keyword.toLowerCase()));

                                                if (isPublic) {
                                                        return;
                                                }
                                                operation.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
                                        }
                                );
                        });
                };
        }

}
