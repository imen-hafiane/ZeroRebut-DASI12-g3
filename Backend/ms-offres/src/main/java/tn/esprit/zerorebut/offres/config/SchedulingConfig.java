package tn.esprit.zerorebut.offres.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Active les tâches planifiées (@Scheduled), utilisées pour l'expiration des offres. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
