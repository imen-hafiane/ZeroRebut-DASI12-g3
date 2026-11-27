package tn.esprit.zerorebut.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.zerorebut.stock.domain.*;
import tn.esprit.zerorebut.stock.dto.ArticleStockRequestDTO;
import tn.esprit.zerorebut.stock.dto.ArticleStockResponseDTO;
import tn.esprit.zerorebut.stock.dto.ReservationStockRequestDTO;
import tn.esprit.zerorebut.stock.dto.StockAlerteDTO;
import tn.esprit.zerorebut.stock.exception.ResourceNotFoundException;
import tn.esprit.zerorebut.stock.exception.StockInsuffisantException;
import tn.esprit.zerorebut.stock.mapper.MouvementMapper;
import tn.esprit.zerorebut.stock.mapper.StockMapper;
import tn.esprit.zerorebut.stock.repository.IArticleStockRepository;
import tn.esprit.zerorebut.stock.repository.IMouvementStockRepository;
import tn.esprit.zerorebut.stock.service.StockServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private IArticleStockRepository articleRepository;

    @Mock
    private IMouvementStockRepository mouvementRepository;

    @Spy
    private StockMapper stockMapper = new StockMapper();

    @Spy
    private MouvementMapper mouvementMapper = new MouvementMapper();

    @InjectMocks
    private StockServiceImpl stockService;

    private ArticleStock article;

    @BeforeEach
    void setUp() {
        article = ArticleStock.builder()
                .id(1L)
                .nom("Pains au chocolat")
                .codeBarre("123456789")
                .categorie(CategorieProduit.BOULANGERIE_PATISSERIE)
                .unite(UniteMesure.PIECE)
                .quantiteDisponible(10.0)
                .quantiteAlerte(3.0)
                .prixOriginal(BigDecimal.valueOf(1.20))
                .datePeremption(LocalDate.now().plusDays(2))
                .statut(StatutArticle.DISPONIBLE)
                .idCommercant(101L)
                .nomMagasin("Boulangerie Centrale")
                .build();
    }

    @Test
    @DisplayName("Création d'un article avec entrée en stock tracée")
    void testCreateArticle() {
        ArticleStockRequestDTO request = ArticleStockRequestDTO.builder()
                .nom("Pains au chocolat")
                .categorie(CategorieProduit.BOULANGERIE_PATISSERIE)
                .unite(UniteMesure.PIECE)
                .quantiteDisponible(10.0)
                .quantiteAlerte(3.0)
                .prixOriginal(BigDecimal.valueOf(1.20))
                .datePeremption(LocalDate.now().plusDays(5))
                .idCommercant(101L)
                .nomMagasin("Boulangerie Centrale")
                .build();

        when(articleRepository.save(any(ArticleStock.class))).thenAnswer(inv -> {
            ArticleStock a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        ArticleStockResponseDTO response = stockService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNom()).isEqualTo("Pains au chocolat");
        verify(mouvementRepository, times(1)).save(any(MouvementStock.class));
    }

    @Test
    @DisplayName("Réserver du stock pour une offre panier anti-gaspillage")
    void testReserverPourOffreSucces() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(ArticleStock.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationStockRequestDTO resRequest = ReservationStockRequestDTO.builder()
                .quantite(4.0)
                .referenceExterne("OFFRE-100")
                .motif("Panier matin")
                .build();

        ArticleStockResponseDTO response = stockService.reserverStockPourOffre(1L, resRequest);

        assertThat(response.getQuantiteDisponible()).isEqualTo(6.0);
        assertThat(response.getStatut()).isEqualTo(StatutArticle.RESERVE_OFFRE);
        verify(mouvementRepository, times(1)).save(any(MouvementStock.class));
    }

    @Test
    @DisplayName("Échec de réservation si stock disponible insuffisant")
    void testReserverPourOffreStockInsuffisant() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ReservationStockRequestDTO resRequest = ReservationStockRequestDTO.builder()
                .quantite(15.0) // plus que les 10 disponibles
                .referenceExterne("OFFRE-100")
                .build();

        assertThrows(StockInsuffisantException.class, () ->
                stockService.reserverStockPourOffre(1L, resRequest)
        );
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Libération d'une réservation de stock")
    void testLibererReservationStock() {
        article.setQuantiteDisponible(6.0);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(ArticleStock.class))).thenAnswer(inv -> inv.getArgument(0));

        ArticleStockResponseDTO response = stockService.libererReservationStock(1L, 4.0, "Annulation offre");

        assertThat(response.getQuantiteDisponible()).isEqualTo(10.0);
        verify(mouvementRepository, times(1)).save(any(MouvementStock.class));
    }

    @Test
    @DisplayName("Mettre un article périmé au rebut")
    void testRebuterArticlePerime() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(ArticleStock.class))).thenAnswer(inv -> inv.getArgument(0));

        ArticleStockResponseDTO response = stockService.rebuterArticlePerime(1L, "DLC passée");

        assertThat(response.getQuantiteDisponible()).isEqualTo(0.0);
        assertThat(response.getStatut()).isEqualTo(StatutArticle.REBUTE);
        verify(mouvementRepository, times(1)).save(any(MouvementStock.class));
    }

    @Test
    @DisplayName("Génération de la synthèse des alertes commerçant")
    void testGetSyntheseAlertesCommercant() {
        when(articleRepository.findArticlesProchesPeremption(eq(101L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(article));
        when(articleRepository.findArticlesEnSeuilCritique(101L)).thenReturn(List.of());
        when(articleRepository.countByIdCommercant(101L)).thenReturn(5L);
        when(articleRepository.countByIdCommercantAndStatut(101L, StatutArticle.PERIME)).thenReturn(0L);

        StockAlerteDTO synthese = stockService.getSyntheseAlertesCommercant(101L, 3);

        assertThat(synthese.getIdCommercant()).isEqualTo(101L);
        assertThat(synthese.getTotalArticlesEnStock()).isEqualTo(5L);
        assertThat(synthese.getNombreArticlesProchesPeremption()).isEqualTo(1L);
        assertThat(synthese.getArticlesProchesPeremption()).hasSize(1);
    }
}
