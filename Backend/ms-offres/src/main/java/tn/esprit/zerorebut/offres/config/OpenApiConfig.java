package tn.esprit.zerorebut.offres.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI : http://localhost:8082/swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI offresOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MS2 · Offres anti-gaspillage")
                .description("ZéroRebut — publication de paniers à prix réduit, cycle de vie des offres, "
                        + "catégories et opérations de réservation appelées par MS3")
                .version("1.0.0"));
    }
}
