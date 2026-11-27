package tn.esprit.zerorebut.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Demande d'allocation / réservation de stock pour une offre ou un don")
public class ReservationStockRequestDTO {

    @NotNull(message = "La quantité à réserver est obligatoire")
    @Positive(message = "La quantité doit être strictement positive")
    @Schema(description = "Quantité demandée", example = "5.0")
    private Double quantite;

    @Schema(description = "Identifiant externe (ex: ID d'offre ou de don)", example = "OFFRE-2026-42")
    private String referenceExterne;

    @Schema(description = "Motif de l'opération", example = "Constitution du panier anti-gaspillage du soir")
    private String motif;

    @Schema(description = "Auteur ou service demandeur", example = "ms-offres")
    private String auteur;
}
