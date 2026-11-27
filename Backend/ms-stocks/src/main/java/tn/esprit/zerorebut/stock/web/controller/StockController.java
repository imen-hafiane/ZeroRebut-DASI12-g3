package tn.esprit.zerorebut.stock.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.dto.*;
import tn.esprit.zerorebut.stock.service.IStockService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/stocks", "/api/stocks"})
@RequiredArgsConstructor
@Tag(name = "MS1 - Stocks", description = "Gestion des stocks alimentaires, détection anti-gaspillage, alertes DLC et réservations")
public class StockController {

    private final IStockService stockService;

    @GetMapping
    @Operation(summary = "Lister tous les articles en stock", description = "Retourne l'ensemble des articles gérés dans le système")
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    public ResponseEntity<List<ArticleStockResponseDTO>> findAll() {
        return ResponseEntity.ok(stockService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un article par son identifiant", description = "Retourne le détail d'un article et son calcul de jours avant péremption")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article trouvé"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé")
    })
    public ResponseEntity<ArticleStockResponseDTO> findById(
            @Parameter(description = "Identifiant de l'article", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(stockService.findById(id));
    }

    @GetMapping("/commercant/{idCommercant}")
    @Operation(summary = "Lister les articles d'un commerçant", description = "Permet de filtrer par statut et/ou catégorie")
    @ApiResponse(responseCode = "200", description = "Liste des articles du commerçant")
    public ResponseEntity<List<ArticleStockResponseDTO>> findByCommercant(
            @PathVariable Long idCommercant,
            @RequestParam(required = false) StatutArticle statut,
            @RequestParam(required = false) CategorieProduit categorie) {
        return ResponseEntity.ok(stockService.findByCommercant(idCommercant, statut, categorie));
    }

    @PostMapping
    @Operation(summary = "Ajouter un nouvel article en stock", description = "Enregistre l'article et trace le mouvement initial d'entrée en stock")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Article créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides (Bean Validation)")
    })
    public ResponseEntity<ArticleStockResponseDTO> create(@Valid @RequestBody ArticleStockRequestDTO requestDTO) {
        ArticleStockResponseDTO created = stockService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un article existant", description = "Met à jour les informations et recalcule l'état critique si nécessaire")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé")
    })
    public ResponseEntity<ArticleStockResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleStockRequestDTO requestDTO) {
        return ResponseEntity.ok(stockService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un article", description = "Supprime un article du stock et son historique associé")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Article supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ENDPOINTS ANTI-GASPILLAGE & ALERTES DLC
    // =========================================================================

    @GetMapping("/commercant/{idCommercant}/alertes-peremption")
    @Operation(summary = "Détecter les articles proches de la péremption (DLC imminente)",
            description = "Filtre les produits à consommer rapidement sous X jours (par défaut 3 jours) éligibles aux offres anti-gaspi ou dons")
    @ApiResponse(responseCode = "200", description = "Liste des articles critiques")
    public ResponseEntity<List<ArticleStockResponseDTO>> getArticlesProchesPeremption(
            @PathVariable Long idCommercant,
            @RequestParam(defaultValue = "3") int jours) {
        return ResponseEntity.ok(stockService.getArticlesProchesPeremption(idCommercant, jours));
    }

    @GetMapping("/commercant/{idCommercant}/seuil-critique")
    @Operation(summary = "Lister les articles en seuil critique de quantité",
            description = "Retourne les articles dont la quantité disponible est inférieure ou égale au seuil d'alerte")
    public ResponseEntity<List<ArticleStockResponseDTO>> getArticlesEnSeuilCritique(@PathVariable Long idCommercant) {
        return ResponseEntity.ok(stockService.getArticlesEnSeuilCritique(idCommercant));
    }

    @GetMapping("/commercant/{idCommercant}/synthese")
    @Operation(summary = "Tableau de bord synthétique des alertes commerçant",
            description = "Agrège les métriques de stock, produits proches de la DLC et ruptures")
    public ResponseEntity<StockAlerteDTO> getSyntheseAlertesCommercant(
            @PathVariable Long idCommercant,
            @RequestParam(defaultValue = "3") int jours) {
        return ResponseEntity.ok(stockService.getSyntheseAlertesCommercant(idCommercant, jours));
    }

    // =========================================================================
    // ENDPOINTS INTER-MICROSERVICES (MS2-Offres, MS4-Dons)
    // =========================================================================

    @PostMapping("/{id}/reserver-offre")
    @Operation(summary = "Réserver du stock pour une offre panier anti-gaspillage",
            description = "Invoqué par le microservice MS2-Offres pour allouer du stock disponible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock alloué avec succès"),
            @ApiResponse(responseCode = "400", description = "Stock insuffisant"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé")
    })
    public ResponseEntity<ArticleStockResponseDTO> reserverStockPourOffre(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStockRequestDTO reservationDTO) {
        return ResponseEntity.ok(stockService.reserverStockPourOffre(id, reservationDTO));
    }

    @PostMapping("/{id}/reserver-don")
    @Operation(summary = "Réserver du stock pour un don à une association",
            description = "Invoqué par le microservice MS4-Dons pour bloquer des denrées alimentaires")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock réservé pour don"),
            @ApiResponse(responseCode = "400", description = "Stock insuffisant"),
            @ApiResponse(responseCode = "404", description = "Article non trouvé")
    })
    public ResponseEntity<ArticleStockResponseDTO> reserverStockPourDon(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStockRequestDTO reservationDTO) {
        return ResponseEntity.ok(stockService.reserverStockPourDon(id, reservationDTO));
    }

    @PostMapping("/{id}/liberer")
    @Operation(summary = "Libérer du stock précédemment réservé",
            description = "Restitue le stock disponible en cas d'annulation d'une offre ou d'un don")
    public ResponseEntity<ArticleStockResponseDTO> libererReservation(
            @PathVariable Long id,
            @RequestParam Double quantite,
            @RequestParam(required = false, defaultValue = "Annulation réservation") String motif) {
        return ResponseEntity.ok(stockService.libererReservationStock(id, quantite, motif));
    }

    @PostMapping("/{id}/rebuter")
    @Operation(summary = "Mettre un article périmé au rebut",
            description = "Passe la quantité disponible à 0 et enregistre le motif de rebut")
    public ResponseEntity<ArticleStockResponseDTO> rebuterArticle(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Péremption constatée") String motif) {
        return ResponseEntity.ok(stockService.rebuterArticlePerime(id, motif));
    }

    @GetMapping("/{id}/mouvements")
    @Operation(summary = "Consulter la traçabilité des mouvements de stock",
            description = "Retourne l'historique complet des entrées, sorties, réservations et rebuts de l'article")
    public ResponseEntity<List<MouvementStockDTO>> getMouvements(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsByArticle(id));
    }
}
