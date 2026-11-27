package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Création d'une offre (statut BROUILLON) avec son panier. */
public record OffreRequest(
        /* Temporaire : sera lu depuis le token Keycloak en S8 */
        @NotBlank(message = "L'identifiant du commerçant est obligatoire")
        String idCommercant,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150)
        String titre,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Le prix initial est obligatoire")
        @Positive
        @Digits(integer = 7, fraction = 3)
        BigDecimal prixInitial,

        @NotNull(message = "Le prix réduit est obligatoire")
        @Positive
        @Digits(integer = 7, fraction = 3)
        BigDecimal prixReduit,

        @NotNull(message = "La date d'expiration est obligatoire")
        @Future(message = "La date d'expiration doit être dans le futur")
        LocalDateTime dateExpiration,

        @NotNull(message = "Le panier est obligatoire")
        @Valid
        PanierRequest panier
) {

    @AssertTrue(message = "Le prix réduit doit être inférieur au prix initial")
    public boolean isPrixCoherent() {
        return prixInitial == null || prixReduit == null || prixReduit.compareTo(prixInitial) < 0;
    }
}
