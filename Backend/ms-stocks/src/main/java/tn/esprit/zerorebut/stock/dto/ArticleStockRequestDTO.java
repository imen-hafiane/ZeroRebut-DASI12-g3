package tn.esprit.zerorebut.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.domain.UniteMesure;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour la création ou mise à jour d'un article en stock")
public class ArticleStockRequestDTO {

    @NotBlank(message = "Le nom de l'article est obligatoire")
    @Size(min = 2, max = 120, message = "Le nom doit comporter entre 2 et 120 caractères")
    @Schema(description = "Nom du produit alimentaire", example = "Croissants pur beurre")
    private String nom;

    @Schema(description = "Code barre EAN ou référence SKU", example = "3560070891234")
    private String codeBarre;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Schema(description = "Description complémentaire", example = "Lot de 6 viennoiseries du matin")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Schema(description = "Catégorie alimentaire", example = "BOULANGERIE_PATISSERIE")
    private CategorieProduit categorie;

    @NotNull(message = "L'unité de mesure est obligatoire")
    @Schema(description = "Unité de mesure", example = "PIECE")
    private UniteMesure unite;

    @NotNull(message = "La quantité disponible est obligatoire")
    @PositiveOrZero(message = "La quantité disponible ne peut pas être négative")
    @Schema(description = "Quantité en stock", example = "15.0")
    private Double quantiteDisponible;

    @NotNull(message = "Le seuil d'alerte de quantité est obligatoire")
    @PositiveOrZero(message = "Le seuil d'alerte ne peut pas être négatif")
    @Schema(description = "Seuil minimal déclenchant l'alerte de réapprovisionnement", example = "3.0")
    private Double quantiteAlerte;

    @NotNull(message = "Le prix original est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix original doit être strictement supérieur à zéro")
    @Schema(description = "Prix de vente initial en dinars (TND)", example = "6.500")
    private BigDecimal prixOriginal;

    @Schema(description = "Date de fabrication ou de mise en rayon", example = "2026-11-18")
    private LocalDate dateFabrication;

    @NotNull(message = "La date limite de consommation (DLC) est obligatoire")
    @Schema(description = "Date limite de consommation (DLC) ou de durabilité minimale", example = "2026-11-22")
    private LocalDate datePeremption;

    @Schema(description = "Statut initial de l'article", example = "DISPONIBLE")
    private StatutArticle statut;

    @NotNull(message = "L'identifiant du commerçant propriétaire est obligatoire")
    @Schema(description = "ID du commerçant dans la plateforme", example = "101")
    private Long idCommercant;

    @NotBlank(message = "Le nom du magasin ou de l'enseigne est obligatoire")
    @Schema(description = "Nom de la boutique ou enseigne", example = "Boulangerie Le Bon Pain")
    private String nomMagasin;

    @Schema(description = "Rayon ou emplacement", example = "Rayon Viennoiseries")
    private String rayonStockage;
}
