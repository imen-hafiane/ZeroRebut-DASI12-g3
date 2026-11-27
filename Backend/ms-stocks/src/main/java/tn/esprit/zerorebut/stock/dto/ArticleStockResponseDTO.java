package tn.esprit.zerorebut.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.domain.UniteMesure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Représentation complète d'un article en stock retourné par l'API")
public class ArticleStockResponseDTO {

    private Long id;
    private String nom;
    private String codeBarre;
    private String description;
    private CategorieProduit categorie;
    private UniteMesure unite;
    private Double quantiteDisponible;
    private Double quantiteAlerte;
    private BigDecimal prixOriginal;
    private LocalDate dateFabrication;
    private LocalDate datePeremption;
    private StatutArticle statut;
    private Long idCommercant;
    private String nomMagasin;
    private String rayonStockage;
    private Long joursRestantsAvantPeremption;
    private Boolean estEnAlerteQuantite;
    private Boolean estProchePeremption;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
}
