// À placer dans Backend/ms-XXX/src/main/java/tn/esprit/zerorebut/xxx/MsXxxApplication.java
package tn.esprit.zerorebut.xxx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsXxxApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsXxxApplication.class, args);
    }
}
