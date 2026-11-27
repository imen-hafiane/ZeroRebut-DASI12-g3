package tn.esprit.zerorebut.stock;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class MsStocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsStocksApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZeroRebut - Microservice Stocks (MS1)")
                        .version("1.0.0")
                        .description("API REST du microservice MS1 : Gestion des stocks alimentaires, détection de péremption, allocation pour paniers anti-gaspi et dons caritatifs.")
                        .contact(new Contact()
                                .name("Équipe ZeroRebut - Groupe 3 DASI12")
                                .email("contact@zerorebut.esprit.tn"))
                        .license(new License().name("Apache 2.0")));
    }
}
