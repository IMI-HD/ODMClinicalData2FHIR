package de.imi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition (
        info = @Info (
                description = "Open Api documentation for ODM to FHIR Converter",
                title = "OpenApi specification",
                version = "1.0"
        ),
        servers = {
                @Server (
                        description = "Local ENV",
                        url = "http://localhost:8080"
                )
        }
)
public class OpenApiConfig {
}
