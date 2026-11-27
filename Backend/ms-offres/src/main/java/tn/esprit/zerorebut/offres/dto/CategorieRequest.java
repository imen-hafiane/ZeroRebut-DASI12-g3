package tn.esprit.zerorebut.offres.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategorieRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "80 caractères maximum")
        String nom,

        @Size(max = 255, message = "255 caractères maximum")
        String description
) {
}
