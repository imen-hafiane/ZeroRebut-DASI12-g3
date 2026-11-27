package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Modification des informations d'une offre (le panier se modifie via ses propres endpoints). */
public record OffreUpdateRequest(
        @NotBlank @Size(max = 150)
        String titre,

        @Size(max = 1000)
        String description,

        @NotNull @Positive @Digits(integer = 7, fraction = 3)
        BigDecimal prixInitial,

        @NotNull @Positive @Digits(integer = 7, fraction = 3)
        BigDecimal prixReduit,

        @NotNull @Future
        LocalDateTime dateExpiration
) {

    @AssertTrue(message = "Le prix réduit doit être inférieur au prix initial")
    public boolean isPrixCoherent() {
        return prixInitial == null || prixReduit == null || prixReduit.compareTo(prixInitial) < 0;
    }
}
