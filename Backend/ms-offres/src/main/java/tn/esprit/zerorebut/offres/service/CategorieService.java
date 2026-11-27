package tn.esprit.zerorebut.offres.service;

import tn.esprit.zerorebut.offres.dto.CategorieRequest;
import tn.esprit.zerorebut.offres.dto.CategorieResponse;

import java.util.List;

public interface CategorieService {

    CategorieResponse creer(CategorieRequest request);

    List<CategorieResponse> lister();

    CategorieResponse obtenir(Long idCategorie);

    CategorieResponse modifier(Long idCategorie, CategorieRequest request);

    void supprimer(Long idCategorie);
}
