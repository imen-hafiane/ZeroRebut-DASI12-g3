package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corps des appels de MS3 : réserver / libérer des paniers. */
public record QuantiteRequest(
        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        Integer quantite
) {
}
