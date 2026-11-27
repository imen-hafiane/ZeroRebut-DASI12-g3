package tn.esprit.zerorebut.offres.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.esprit.zerorebut.offres.dto.CategorieRequest;
import tn.esprit.zerorebut.offres.dto.CategorieResponse;
import tn.esprit.zerorebut.offres.service.CategorieService;

import java.net.URI;
import java.util.List;

@Tag(name = "Catégories", description = "Catégories des paniers")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @Operation(summary = "Créer une catégorie")
    @PostMapping
    public ResponseEntity<CategorieResponse> creer(@Valid @RequestBody CategorieRequest request) {
        CategorieResponse created = categorieService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.idCategorie()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Lister les catégories")
    @GetMapping
    public List<CategorieResponse> lister() {
        return categorieService.lister();
    }

    @Operation(summary = "Consulter une catégorie")
    @GetMapping("/{id}")
    public CategorieResponse obtenir(@PathVariable Long id) {
        return categorieService.obtenir(id);
    }

    @Operation(summary = "Modifier une catégorie")
    @PutMapping("/{id}")
    public CategorieResponse modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return categorieService.modifier(id, request);
    }

    @Operation(summary = "Supprimer une catégorie non utilisée")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        categorieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
