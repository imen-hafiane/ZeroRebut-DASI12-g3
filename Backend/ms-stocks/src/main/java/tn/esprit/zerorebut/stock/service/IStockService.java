package tn.esprit.zerorebut.stock.service;

import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.dto.*;

import java.util.List;

public interface IStockService {

    List<ArticleStockResponseDTO> findAll();

    ArticleStockResponseDTO findById(Long id);

    List<ArticleStockResponseDTO> findByCommercant(Long idCommercant, StatutArticle statut, CategorieProduit categorie);

    ArticleStockResponseDTO create(ArticleStockRequestDTO requestDTO);

    ArticleStockResponseDTO update(Long id, ArticleStockRequestDTO requestDTO);

    void delete(Long id);

    // Services Anti-Gaspillage & Alertes
    List<ArticleStockResponseDTO> getArticlesProchesPeremption(Long idCommercant, int jours);

    List<ArticleStockResponseDTO> getArticlesEnSeuilCritique(Long idCommercant);

    StockAlerteDTO getSyntheseAlertesCommercant(Long idCommercant, int jours);

    // Opérations Inter-Microservices (pour MS2-Offres et MS4-Dons)
    ArticleStockResponseDTO reserverStockPourOffre(Long idArticle, ReservationStockRequestDTO reservationDTO);

    ArticleStockResponseDTO reserverStockPourDon(Long idArticle, ReservationStockRequestDTO reservationDTO);

    ArticleStockResponseDTO libererReservationStock(Long idArticle, Double quantite, String motif);

    ArticleStockResponseDTO rebuterArticlePerime(Long idArticle, String motif);

    // Historique des mouvements
    List<MouvementStockDTO> getMouvementsByArticle(Long idArticle);
}
