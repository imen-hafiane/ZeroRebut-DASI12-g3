package tn.esprit.zerorebut.offres.dto;

import java.util.List;

public record PanierResponse(
        Long idPanier,
        String nom,
        String description,
        Integer quantite,
        CategorieResponse categorie,
        List<LignePanierResponse> lignes
) {
}
