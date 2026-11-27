package tn.esprit.zerorebut.offres.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.zerorebut.offres.domain.*;
import tn.esprit.zerorebut.offres.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class OffreMapper {

    private final CategorieMapper categorieMapper;

    /** La catégorie est résolue par le service (vérification d'existence). */
    public Offre toEntity(OffreRequest request, Categorie categorie) {
        Panier panier = toPanier(request.panier(), categorie);
        return Offre.builder()
                .idCommercant(request.idCommercant())
                .titre(request.titre())
                .description(request.description())
                .prixInitial(request.prixInitial())
                .prixReduit(request.prixReduit())
                .dateExpiration(request.dateExpiration())
                .panier(panier)
                .build();
    }

    public void update(Offre offre, OffreUpdateRequest request) {
        offre.setTitre(request.titre());
        offre.setDescription(request.description());
        offre.setPrixInitial(request.prixInitial());
        offre.setPrixReduit(request.prixReduit());
        offre.setDateExpiration(request.dateExpiration());
    }

    public Panier toPanier(PanierRequest request, Categorie categorie) {
        Panier panier = Panier.builder()
                .nom(request.nom())
                .description(request.description())
                .quantite(request.quantite())
                .categorie(categorie)
                .build();
        request.lignes().forEach(l -> panier.ajouterLigne(toLigne(l)));
        return panier;
    }

    public LignePanier toLigne(LignePanierRequest request) {
        return LignePanier.builder()
                .idProduit(request.idProduit())
                .quantite(request.quantite())
                .build();
    }

    public OffreResponse toResponse(Offre offre) {
        return new OffreResponse(
                offre.getIdOffre(),
                offre.getIdCommercant(),
                offre.getTitre(),
                offre.getDescription(),
                offre.getPrixInitial(),
                offre.getPrixReduit(),
                pourcentageReduction(offre.getPrixInitial(), offre.getPrixReduit()),
                offre.getQuantiteDisponible(),
                offre.getDatePublication(),
                offre.getDateExpiration(),
                offre.getStatut(),
                toResponse(offre.getPanier()));
    }

    public PanierResponse toResponse(Panier panier) {
        return new PanierResponse(
                panier.getIdPanier(),
                panier.getNom(),
                panier.getDescription(),
                panier.getQuantite(),
                categorieMapper.toResponse(panier.getCategorie()),
                panier.getLignes().stream()
                        .map(l -> new LignePanierResponse(l.getIdLigne(), l.getIdProduit(), l.getQuantite()))
                        .toList());
    }

    public DisponibiliteResponse toDisponibilite(Offre offre, int quantiteDemandee) {
        return new DisponibiliteResponse(
                offre.getIdOffre(),
                offre.verifierDisponibilite(quantiteDemandee),
                offre.getQuantiteDisponible(),
                offre.getPrixReduit());
    }

    /** Ex. 12 DT -> 4 DT = 67 % de réduction. */
    private Integer pourcentageReduction(BigDecimal initial, BigDecimal reduit) {
        if (initial == null || reduit == null || initial.signum() == 0) {
            return null;
        }
        return BigDecimal.ONE
                .subtract(reduit.divide(initial, 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}
