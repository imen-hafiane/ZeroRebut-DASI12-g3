package tn.esprit.zerorebut.offres.dto;

import tn.esprit.zerorebut.offres.domain.StatutOffre;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OffreResponse(
        Long idOffre,
        String idCommercant,
        String titre,
        String description,
        BigDecimal prixInitial,
        BigDecimal prixReduit,
        Integer pourcentageReduction,
        Integer quantiteDisponible,
        LocalDateTime datePublication,
        LocalDateTime dateExpiration,
        StatutOffre statut,
        PanierResponse panier
) {
}
