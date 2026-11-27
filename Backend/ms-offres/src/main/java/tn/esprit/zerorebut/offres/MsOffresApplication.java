package tn.esprit.zerorebut.offres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsOffresApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsOffresApplication.class, args);
    }
}
