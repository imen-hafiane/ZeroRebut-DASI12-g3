package tn.esprit.zerorebut.offres.dto;

import java.math.BigDecimal;

/** Réponse légère pour MS3 (OpenFeign) : l'offre peut-elle être réservée ? */
public record DisponibiliteResponse(
        Long idOffre,
        boolean disponible,
        Integer quantiteDisponible,
        BigDecimal prixReduit
) {
}
