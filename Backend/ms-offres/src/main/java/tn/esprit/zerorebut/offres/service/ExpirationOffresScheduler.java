package tn.esprit.zerorebut.offres.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Passe automatiquement à EXPIREE les offres dont la date limite est dépassée.
 * Intervalle configurable : offres.expiration.intervalle-ms (1 minute par défaut).
 */
@Component
@RequiredArgsConstructor
public class ExpirationOffresScheduler {

    private final OffreService offreService;

    @Scheduled(fixedDelayString = "${offres.expiration.intervalle-ms:60000}", initialDelay = 10000)
    public void expirerOffres() {
        offreService.expirerOffresDepassees();
    }
}
