package tn.esprit.zerorebut.offres.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests des règles métier de l'agrégat Offre (sans Spring, très rapides). */
class OffreTest {

    static Offre offreBrouillon(int nombrePaniers) {
        Panier panier = Panier.builder()
                .nom("Panier viennoiseries")
                .quantite(nombrePaniers)
                .categorie(Categorie.builder().idCategorie(1L).nom("Boulangerie").build())
                .build();
        panier.ajouterLigne(LignePanier.builder().idProduit(10L).quantite(3).build());
        return Offre.builder()
                .idCommercant("commercant-001")
                .titre("Panier surprise")
                .prixInitial(new BigDecimal("12.000"))
                .prixReduit(new BigDecimal("4.000"))
                .dateExpiration(LocalDateTime.now().plusDays(1))
                .panier(panier)
                .build();
    }

    @Test
    @DisplayName("Une nouvelle offre est en BROUILLON")
    void nouvelleOffre_estEnBrouillon() {
        assertThat(offreBrouillon(5).getStatut()).isEqualTo(StatutOffre.BROUILLON);
    }

    @Test
    @DisplayName("Publier un brouillon valide -> DISPONIBLE avec toute la quantité")
    void publier_brouillonValide() {
        Offre offre = offreBrouillon(5);
        offre.publier();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
        assertThat(offre.getQuantiteDisponible()).isEqualTo(5);
        assertThat(offre.getDatePublication()).isNotNull();
    }

    @Test
    @DisplayName("Publier refuse un prix réduit >= prix initial")
    void publier_prixIncoherent() {
        Offre offre = offreBrouillon(5);
        offre.setPrixReduit(new BigDecimal("15.000"));
        assertThatThrownBy(offre::publier)
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("prix réduit");
    }

    @Test
    @DisplayName("Publier refuse un panier vide")
    void publier_panierVide() {
        Offre offre = offreBrouillon(5);
        offre.getPanier().getLignes().clear();
        assertThatThrownBy(offre::publier).isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("Publier refuse une date d'expiration dépassée")
    void publier_dateDepassee() {
        Offre offre = offreBrouillon(5);
        offre.setDateExpiration(LocalDateTime.now().minusHours(1));
        assertThatThrownBy(offre::publier).isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("On ne publie pas deux fois la même offre")
    void publier_dejaDisponible() {
        Offre offre = offreBrouillon(5);
        offre.publier();
        assertThatThrownBy(offre::publier).isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("Réserver tous les paniers -> EPUISEE")
    void reserver_tout() {
        Offre offre = offreBrouillon(3);
        offre.publier();
        offre.reserver(3);
        assertThat(offre.getQuantiteDisponible()).isZero();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.EPUISEE);
    }

    @Test
    @DisplayName("Réserver plus que disponible est refusé")
    void reserver_quantiteInsuffisante() {
        Offre offre = offreBrouillon(3);
        offre.publier();
        assertThatThrownBy(() -> offre.reserver(4)).isInstanceOf(RegleMetierException.class);
        assertThat(offre.getQuantiteDisponible()).isEqualTo(3);
    }

    @Test
    @DisplayName("Impossible de réserver une offre non publiée")
    void reserver_brouillon() {
        Offre offre = offreBrouillon(3);
        assertThatThrownBy(() -> offre.reserver(1)).isInstanceOf(RegleMetierException.class);
    }

    @Test
    @DisplayName("Libérer des paniers d'une offre épuisée -> DISPONIBLE")
    void liberer_offreEpuisee() {
        Offre offre = offreBrouillon(2);
        offre.publier();
        offre.reserver(2);
        offre.liberer(1);
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
        assertThat(offre.getQuantiteDisponible()).isEqualTo(1);
    }

    @Test
    @DisplayName("Libérer ne dépasse jamais la quantité initiale")
    void liberer_plafonne() {
        Offre offre = offreBrouillon(2);
        offre.publier();
        offre.liberer(5);
        assertThat(offre.getQuantiteDisponible()).isEqualTo(2);
    }

    @Test
    @DisplayName("Suspendre puis republier conserve la quantité restante")
    void suspendre_puisRepublier() {
        Offre offre = offreBrouillon(5);
        offre.publier();
        offre.reserver(2);
        offre.suspendre();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.SUSPENDUE);
        offre.publier();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
        assertThat(offre.getQuantiteDisponible()).isEqualTo(3);
    }

    @Test
    @DisplayName("Une offre dont la date est dépassée passe à EXPIREE")
    void expirer_dateDepassee() {
        Offre offre = offreBrouillon(5);
        offre.publier();
        offre.setDateExpiration(LocalDateTime.now().minusMinutes(1));
        assertThat(offre.expirerSiNecessaire()).isTrue();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.EXPIREE);
    }

    @Test
    @DisplayName("Une offre annulée ne peut plus être désactivée ni modifiée")
    void annulee_estFinale() {
        Offre offre = offreBrouillon(5);
        offre.desactiver();
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.ANNULEE);
        assertThatThrownBy(offre::desactiver).isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(offre::verifierModifiable).isInstanceOf(RegleMetierException.class);
    }
}
