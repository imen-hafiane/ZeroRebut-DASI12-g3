package tn.esprit.zerorebut.offres.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.zerorebut.offres.domain.StatutOffre;
import tn.esprit.zerorebut.offres.dto.*;

import java.math.BigDecimal;

public interface OffreService {

    // ---------- CRUD ----------
    OffreResponse creer(OffreRequest request);

    OffreResponse obtenir(Long idOffre);

    Page<OffreResponse> rechercher(StatutOffre statut, Long idCategorie, BigDecimal prixMax,
                                   String idCommercant, Pageable pageable);

    OffreResponse modifier(Long idOffre, OffreUpdateRequest request);

    void supprimer(Long idOffre);

    // ---------- Cycle de vie ----------
    OffreResponse publier(Long idOffre);

    OffreResponse suspendre(Long idOffre);

    OffreResponse desactiver(Long idOffre);

    int expirerOffresDepassees();

    // ---------- Panier (partie de l'agrégat Offre) ----------
    PanierResponse obtenirPanier(Long idOffre);

    PanierResponse ajouterLigne(Long idOffre, LignePanierRequest request);

    PanierResponse retirerLigne(Long idOffre, Long idLigne);

    // ---------- Appels de MS3 · Réservations (OpenFeign en S6) ----------
    DisponibiliteResponse verifierDisponibilite(Long idOffre, int quantite);

    OffreResponse reserver(Long idOffre, int quantite);

    OffreResponse liberer(Long idOffre, int quantite);
}
