package tn.esprit.zerorebut.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import tn.esprit.zerorebut.stock.domain.TypeMouvement;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Historique d'un mouvement de stock (entrée, vente, réservation, don, rebut)")
public class MouvementStockDTO {

    private Long id;
    private Long articleId;
    private String articleNom;
    private TypeMouvement type;
    private Double quantite;
    private LocalDateTime dateMouvement;
    private String referenceExterne;
    private String motif;
    private String auteur;
}
