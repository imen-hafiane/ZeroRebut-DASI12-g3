package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PanierRequest(
        @NotBlank(message = "Le nom du panier est obligatoire")
        @Size(max = 120)
        String nom,

        @Size(max = 500)
        String description,

        @NotNull(message = "Le nombre de paniers est obligatoire")
        @Positive(message = "Le nombre de paniers doit être positif")
        Integer quantite,

        @NotNull(message = "La catégorie est obligatoire")
        Long idCategorie,

        @NotEmpty(message = "Le panier doit contenir au moins un produit")
        List<@Valid LignePanierRequest> lignes
) {
}
