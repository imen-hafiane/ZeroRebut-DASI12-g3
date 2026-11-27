package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LignePanierRequest(
        @NotNull(message = "L'identifiant du produit (MS1) est obligatoire")
        @Positive
        Long idProduit,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        Integer quantite
) {
}
