package tn.esprit.zerorebut.offres.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.esprit.zerorebut.offres.domain.StatutOffre;
import tn.esprit.zerorebut.offres.dto.*;
import tn.esprit.zerorebut.offres.service.OffreService;

import java.math.BigDecimal;
import java.net.URI;

@Tag(name = "Offres", description = "Offres anti-gaspillage et leur cycle de vie")
@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    // ---------- CRUD ----------

    @Operation(summary = "Créer une offre (statut BROUILLON) avec son panier")
    @PostMapping
    public ResponseEntity<OffreResponse> creer(@Valid @RequestBody OffreRequest request) {
        OffreResponse created = offreService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.idOffre()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Rechercher des offres (filtres optionnels, paginé)")
    @GetMapping
    public PageResponse<OffreResponse> rechercher(
            @Parameter(description = "BROUILLON, DISPONIBLE, SUSPENDUE, EPUISEE, EXPIREE, ANNULEE")
            @RequestParam(required = false) StatutOffre statut,
            @Parameter(description = "Identifiant de la catégorie")
            @RequestParam(required = false) Long categorie,
            @Parameter(description = "Prix réduit maximum")
            @RequestParam(required = false) BigDecimal prixMax,
            @Parameter(description = "Identifiant du commerçant")
            @RequestParam(required = false) String commercant,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dateExpiration", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(offreService.rechercher(statut, categorie, prixMax, commercant, pageable));
    }

    @Operation(summary = "Consulter une offre")
    @GetMapping("/{id}")
    public OffreResponse obtenir(@PathVariable Long id) {
        return offreService.obtenir(id);
    }

    @Operation(summary = "Modifier les informations d'une offre")
    @PutMapping("/{id}")
    public OffreResponse modifier(@PathVariable Long id, @Valid @RequestBody OffreUpdateRequest request) {
        return offreService.modifier(id, request);
    }

    @Operation(summary = "Supprimer une offre (brouillon ou annulée uniquement)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Cycle de vie ----------

    @Operation(summary = "Publier une offre (BROUILLON ou SUSPENDUE -> DISPONIBLE)")
    @PutMapping("/{id}/publier")
    public OffreResponse publier(@PathVariable Long id) {
        return offreService.publier(id);
    }

    @Operation(summary = "Suspendre une offre disponible")
    @PutMapping("/{id}/suspendre")
    public OffreResponse suspendre(@PathVariable Long id) {
        return offreService.suspendre(id);
    }

    @Operation(summary = "Désactiver définitivement une offre (-> ANNULEE)")
    @PutMapping("/{id}/desactiver")
    public OffreResponse desactiver(@PathVariable Long id) {
        return offreService.desactiver(id);
    }

    // ---------- Panier ----------

    @Operation(summary = "Consulter le panier d'une offre")
    @GetMapping("/{id}/panier")
    public PanierResponse obtenirPanier(@PathVariable Long id) {
        return offreService.obtenirPanier(id);
    }

    @Operation(summary = "Ajouter un produit au panier (offre en brouillon)")
    @PostMapping("/{id}/panier/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    public PanierResponse ajouterLigne(@PathVariable Long id, @Valid @RequestBody LignePanierRequest request) {
        return offreService.ajouterLigne(id, request);
    }

    @Operation(summary = "Retirer un produit du panier (offre en brouillon)")
    @DeleteMapping("/{id}/panier/lignes/{idLigne}")
    public PanierResponse retirerLigne(@PathVariable Long id, @PathVariable Long idLigne) {
        return offreService.retirerLigne(id, idLigne);
    }

    // ---------- Appels de MS3 · Réservations ----------

    @Operation(summary = "Vérifier la disponibilité (appelé par MS3)")
    @GetMapping("/{id}/disponibilite")
    public DisponibiliteResponse verifierDisponibilite(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") @Positive Integer quantite) {
        return offreService.verifierDisponibilite(id, quantite);
    }

    @Operation(summary = "Réserver des paniers (appelé par MS3)")
    @PutMapping("/{id}/reserver")
    public OffreResponse reserver(@PathVariable Long id, @Valid @RequestBody QuantiteRequest request) {
        return offreService.reserver(id, request.quantite());
    }

    @Operation(summary = "Libérer des paniers après annulation (appelé par MS3)")
    @PutMapping("/{id}/liberer")
    public OffreResponse liberer(@PathVariable Long id, @Valid @RequestBody QuantiteRequest request) {
        return offreService.liberer(id, request.quantite());
    }
}
