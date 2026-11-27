package tn.esprit.zerorebut.stock.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import tn.esprit.zerorebut.stock.repository.IArticleStockRepository;

import java.time.LocalDate;

/**
 * HealthIndicator personnalisé pour l'observabilité métier (Chapitre 3 : Actuator).
 * Évalue la santé du système de stock et remonte des métriques vers /actuator/health.
 */
@Component
@RequiredArgsConstructor
public class StockHealthIndicator implements HealthIndicator {

    private final IArticleStockRepository articleRepository;

    @Override
    public Health health() {
        try {
            long totalArticles = articleRepository.count();
            LocalDate aujourdhui = LocalDate.now();
            long articlesCritiques = articleRepository.findAllProchesPeremption(aujourdhui, aujourdhui.plusDays(2)).size();

            return Health.up()
                    .withDetail("service", "ms-stocks")
                    .withDetail("status", "OPERATIONAL")
                    .withDetail("totalArticlesGeres", totalArticles)
                    .withDetail("articlesCritiquesDlc48h", articlesCritiques)
                    .withDetail("message", "Le gestionnaire de stock anti-gaspillage fonctionne normalement.")
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("service", "ms-stocks")
                    .withDetail("status", "DEGRADED")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
