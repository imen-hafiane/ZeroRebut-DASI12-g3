package tn.esprit.zerorebut.offres.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.zerorebut.offres.domain.*;
import tn.esprit.zerorebut.offres.dto.*;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;
import tn.esprit.zerorebut.offres.exception.RessourceIntrouvableException;
import tn.esprit.zerorebut.offres.mapper.OffreMapper;
import tn.esprit.zerorebut.offres.repository.CategorieRepository;
import tn.esprit.zerorebut.offres.repository.OffreRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Orchestration des cas d'utilisation du MS2.
 * Les règles de cycle de vie sont dans l'entité Offre (agrégat racine) ;
 * le service charge, délègue à l'entité, puis renvoie un DTO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OffreServiceImpl implements OffreService {

    private final OffreRepository offreRepository;
    private final CategorieRepository categorieRepository;
    private final OffreMapper mapper;

    // ---------- CRUD ----------

    @Override
    public OffreResponse creer(OffreRequest request) {
        Categorie categorie = trouverCategorie(request.panier().idCategorie());
        Offre offre = offreRepository.save(mapper.toEntity(request, categorie));
        log.info("Offre créée en brouillon : {} (commerçant {})", offre.getIdOffre(), offre.getIdCommercant());
        return mapper.toResponse(offre);
    }

    @Override
    @Transactional(readOnly = true)
    public OffreResponse obtenir(Long idOffre) {
        return mapper.toResponse(trouver(idOffre));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OffreResponse> rechercher(StatutOffre statut, Long idCategorie, BigDecimal prixMax,
                                          String idCommercant, Pageable pageable) {
        return offreRepository.rechercher(statut, idCategorie, prixMax, idCommercant, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public OffreResponse modifier(Long idOffre, OffreUpdateRequest request) {
        Offre offre = trouver(idOffre);
        offre.verifierModifiable();
        mapper.update(offre, request);
        return mapper.toResponse(offre);
    }

    /** Suppression physique seulement pour un brouillon ou une offre annulée : sinon, on désactive. */
    @Override
    public void supprimer(Long idOffre) {
        Offre offre = trouver(idOffre);
        if (offre.getStatut() != StatutOffre.BROUILLON && offre.getStatut() != StatutOffre.ANNULEE) {
            throw new RegleMetierException("Seule une offre en brouillon ou annulée peut être supprimée : désactivez-la d'abord");
        }
        offreRepository.delete(offre);
        log.info("Offre supprimée : {}", idOffre);
    }

    // ---------- Cycle de vie ----------

    @Override
    public OffreResponse publier(Long idOffre) {
        Offre offre = trouver(idOffre);
        offre.publier();
        log.info("Offre publiée : {} ({} paniers)", idOffre, offre.getQuantiteDisponible());
        // S6 : réserver les produits dans MS1 (OpenFeign)
        // S7 : publier l'événement OffrePubliee (RabbitMQ)
        return mapper.toResponse(offre);
    }

    @Override
    public OffreResponse suspendre(Long idOffre) {
        Offre offre = trouver(idOffre);
        offre.suspendre();
        log.info("Offre suspendue : {}", idOffre);
        // S7 : publier l'événement OffreSuspendue
        return mapper.toResponse(offre);
    }

    @Override
    public OffreResponse desactiver(Long idOffre) {
        Offre offre = trouver(idOffre);
        offre.desactiver();
        log.info("Offre désactivée : {}", idOffre);
        // S6 : libérer les produits réservés dans MS1
        return mapper.toResponse(offre);
    }

    /** Appelée par ExpirationOffresScheduler. */
    @Override
    public int expirerOffresDepassees() {
        List<Offre> candidates = offreRepository.findByStatutInAndDateExpirationBefore(
                EnumSet.of(StatutOffre.BROUILLON, StatutOffre.DISPONIBLE, StatutOffre.SUSPENDUE, StatutOffre.EPUISEE),
                LocalDateTime.now());
        int nombre = 0;
        for (Offre offre : candidates) {
            if (offre.expirerSiNecessaire()) {
                nombre++;
            }
        }
        if (nombre > 0) {
            log.info("{} offre(s) passée(s) à EXPIREE", nombre);
        }
        return nombre;
    }

    // ---------- Panier ----------

    @Override
    @Transactional(readOnly = true)
    public PanierResponse obtenirPanier(Long idOffre) {
        return mapper.toResponse(trouver(idOffre).getPanier());
    }

    @Override
    public PanierResponse ajouterLigne(Long idOffre, LignePanierRequest request) {
        Offre offre = trouverBrouillon(idOffre);
        offre.getPanier().ajouterLigne(mapper.toLigne(request));
        offreRepository.flush(); // génère l'id de la nouvelle ligne avant la réponse
        return mapper.toResponse(offre.getPanier());
    }

    @Override
    public PanierResponse retirerLigne(Long idOffre, Long idLigne) {
        Offre offre = trouverBrouillon(idOffre);
        Panier panier = offre.getPanier();
        LignePanier ligne = panier.getLignes().stream()
                .filter(l -> l.getIdLigne().equals(idLigne))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Ligne de panier", idLigne));
        if (panier.getLignes().size() == 1) {
            throw new RegleMetierException("Le panier doit garder au moins un produit");
        }
        panier.retirerLigne(ligne);
        return mapper.toResponse(panier);
    }

    // ---------- Appels de MS3 ----------

    @Override
    @Transactional(readOnly = true)
    public DisponibiliteResponse verifierDisponibilite(Long idOffre, int quantite) {
        return mapper.toDisponibilite(trouver(idOffre), quantite);
    }

    @Override
    public OffreResponse reserver(Long idOffre, int quantite) {
        Offre offre = trouver(idOffre);
        offre.reserver(quantite);
        log.info("Offre {} : {} panier(s) réservé(s), reste {}", idOffre, quantite, offre.getQuantiteDisponible());
        if (offre.getStatut() == StatutOffre.EPUISEE) {
            log.info("Offre {} épuisée", idOffre);
            // S7 : publier l'événement OffreEpuisee
        }
        return mapper.toResponse(offre);
    }

    @Override
    public OffreResponse liberer(Long idOffre, int quantite) {
        Offre offre = trouver(idOffre);
        offre.liberer(quantite);
        log.info("Offre {} : {} panier(s) libéré(s), reste {}", idOffre, quantite, offre.getQuantiteDisponible());
        return mapper.toResponse(offre);
    }

    // ---------- Utilitaires ----------

    private Offre trouver(Long idOffre) {
        return offreRepository.findById(idOffre)
                .orElseThrow(() -> new RessourceIntrouvableException("Offre", idOffre));
    }

    private Categorie trouverCategorie(Long idCategorie) {
        return categorieRepository.findById(idCategorie)
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie", idCategorie));
    }

    /** Le contenu du panier n'est modifiable qu'avant publication. */
    private Offre trouverBrouillon(Long idOffre) {
        Offre offre = trouver(idOffre);
        if (offre.getStatut() != StatutOffre.BROUILLON) {
            throw new RegleMetierException("Le contenu du panier n'est modifiable que pour une offre en brouillon");
        }
        return offre;
    }
}
