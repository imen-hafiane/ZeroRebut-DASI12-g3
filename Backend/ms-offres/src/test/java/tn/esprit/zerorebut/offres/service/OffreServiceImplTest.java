package tn.esprit.zerorebut.offres.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.zerorebut.offres.domain.Categorie;
import tn.esprit.zerorebut.offres.domain.Offre;
import tn.esprit.zerorebut.offres.domain.StatutOffre;
import tn.esprit.zerorebut.offres.dto.*;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;
import tn.esprit.zerorebut.offres.exception.RessourceIntrouvableException;
import tn.esprit.zerorebut.offres.mapper.CategorieMapper;
import tn.esprit.zerorebut.offres.mapper.OffreMapper;
import tn.esprit.zerorebut.offres.repository.CategorieRepository;
import tn.esprit.zerorebut.offres.repository.OffreRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/** Tests du service avec des repositories simulés (Mockito) : pas de base de données. */
@ExtendWith(MockitoExtension.class)
class OffreServiceImplTest {

    @Mock
    private OffreRepository offreRepository;
    @Mock
    private CategorieRepository categorieRepository;

    private OffreServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OffreServiceImpl(offreRepository, categorieRepository, new OffreMapper(new CategorieMapper()));
    }

    private OffreRequest requete(long idCategorie) {
        return new OffreRequest("commercant-001", "Panier surprise", null,
                new BigDecimal("12"), new BigDecimal("4"), LocalDateTime.now().plusDays(1),
                new PanierRequest("Panier", null, 5, idCategorie, List.of(new LignePanierRequest(10L, 3))));
    }

    @Test
    @DisplayName("Créer une offre -> BROUILLON avec un pourcentage de réduction calculé")
    void creer_ok() {
        when(categorieRepository.findById(1L))
                .thenReturn(Optional.of(Categorie.builder().idCategorie(1L).nom("Boulangerie").build()));
        when(offreRepository.save(any(Offre.class))).thenAnswer(inv -> inv.getArgument(0));

        OffreResponse response = service.creer(requete(1L));

        assertThat(response.statut()).isEqualTo(StatutOffre.BROUILLON);
        assertThat(response.pourcentageReduction()).isEqualTo(67);
        assertThat(response.panier().lignes()).hasSize(1);
        verify(offreRepository).save(any(Offre.class));
    }

    @Test
    @DisplayName("Créer une offre avec une catégorie inexistante -> 404")
    void creer_categorieInexistante() {
        when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(requete(99L)))
                .isInstanceOf(RessourceIntrouvableException.class);
        verify(offreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Réserver une offre inexistante -> 404")
    void reserver_offreInexistante() {
        when(offreRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserver(42L, 1))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("Supprimer une offre publiée est refusé")
    void supprimer_offrePubliee() {
        Offre offre = offrePubliee();
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));

        assertThatThrownBy(() -> service.supprimer(1L)).isInstanceOf(RegleMetierException.class);
        verify(offreRepository, never()).delete(any());
    }

    @Test
    @DisplayName("L'expiration planifiée compte les offres expirées")
    void expirer_compte() {
        Offre expiree = offrePubliee();
        expiree.setDateExpiration(LocalDateTime.now().minusMinutes(5));
        when(offreRepository.findByStatutInAndDateExpirationBefore(anyCollection(), any()))
                .thenReturn(List.of(expiree));

        assertThat(service.expirerOffresDepassees()).isEqualTo(1);
        assertThat(expiree.getStatut()).isEqualTo(StatutOffre.EXPIREE);
    }

    private Offre offrePubliee() {
        Offre offre = new OffreMapper(new CategorieMapper())
                .toEntity(requete(1L), Categorie.builder().idCategorie(1L).nom("Boulangerie").build());
        offre.publier();
        return offre;
    }
}
