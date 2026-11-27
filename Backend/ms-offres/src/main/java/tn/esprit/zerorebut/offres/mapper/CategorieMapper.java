package tn.esprit.zerorebut.offres.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.zerorebut.offres.domain.Categorie;
import tn.esprit.zerorebut.offres.dto.CategorieRequest;
import tn.esprit.zerorebut.offres.dto.CategorieResponse;

@Component
public class CategorieMapper {

    public Categorie toEntity(CategorieRequest request) {
        return Categorie.builder()
                .nom(request.nom().trim())
                .description(request.description())
                .build();
    }

    public void update(Categorie categorie, CategorieRequest request) {
        categorie.setNom(request.nom().trim());
        categorie.setDescription(request.description());
    }

    public CategorieResponse toResponse(Categorie categorie) {
        return new CategorieResponse(categorie.getIdCategorie(), categorie.getNom(), categorie.getDescription());
    }
}
