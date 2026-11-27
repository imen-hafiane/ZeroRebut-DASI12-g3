package tn.esprit.zerorebut.offres.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.zerorebut.offres.domain.Categorie;
import tn.esprit.zerorebut.offres.dto.CategorieRequest;
import tn.esprit.zerorebut.offres.dto.CategorieResponse;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;
import tn.esprit.zerorebut.offres.exception.RessourceIntrouvableException;
import tn.esprit.zerorebut.offres.mapper.CategorieMapper;
import tn.esprit.zerorebut.offres.repository.CategorieRepository;
import tn.esprit.zerorebut.offres.repository.OffreRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final OffreRepository offreRepository;
    private final CategorieMapper mapper;

    @Override
    public CategorieResponse creer(CategorieRequest request) {
        if (categorieRepository.existsByNomIgnoreCase(request.nom().trim())) {
            throw new RegleMetierException("La catégorie '" + request.nom() + "' existe déjà");
        }
        Categorie categorie = categorieRepository.save(mapper.toEntity(request));
        log.info("Catégorie créée : {} ({})", categorie.getNom(), categorie.getIdCategorie());
        return mapper.toResponse(categorie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponse> lister() {
        return categorieRepository.findAll(Sort.by("nom")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieResponse obtenir(Long idCategorie) {
        return mapper.toResponse(trouver(idCategorie));
    }

    @Override
    public CategorieResponse modifier(Long idCategorie, CategorieRequest request) {
        Categorie categorie = trouver(idCategorie);
        boolean nomChange = !categorie.getNom().equalsIgnoreCase(request.nom().trim());
        if (nomChange && categorieRepository.existsByNomIgnoreCase(request.nom().trim())) {
            throw new RegleMetierException("La catégorie '" + request.nom() + "' existe déjà");
        }
        mapper.update(categorie, request);
        return mapper.toResponse(categorie);
    }

    @Override
    public void supprimer(Long idCategorie) {
        Categorie categorie = trouver(idCategorie);
        if (offreRepository.existsByPanierCategorieIdCategorie(idCategorie)) {
            throw new RegleMetierException("Impossible de supprimer une catégorie utilisée par des offres");
        }
        categorieRepository.delete(categorie);
        log.info("Catégorie supprimée : {}", idCategorie);
    }

    private Categorie trouver(Long idCategorie) {
        return categorieRepository.findById(idCategorie)
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie", idCategorie));
    }
}
