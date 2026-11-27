package tn.esprit.zerorebut.stock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.zerorebut.stock.domain.ArticleStock;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.MouvementStock;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.domain.TypeMouvement;
import tn.esprit.zerorebut.stock.dto.*;
import tn.esprit.zerorebut.stock.exception.BusinessException;
import tn.esprit.zerorebut.stock.exception.ResourceNotFoundException;
import tn.esprit.zerorebut.stock.exception.StockInsuffisantException;
import tn.esprit.zerorebut.stock.mapper.MouvementMapper;
import tn.esprit.zerorebut.stock.mapper.StockMapper;
import tn.esprit.zerorebut.stock.repository.IArticleStockRepository;
import tn.esprit.zerorebut.stock.repository.IMouvementStockRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements IStockService {

    private final IArticleStockRepository articleRepository;
    private final IMouvementStockRepository mouvementRepository;
    private final StockMapper stockMapper;
    private final MouvementMapper mouvementMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ArticleStockResponseDTO> findAll() {
        return articleRepository.findAll().stream()
                .map(stockMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleStockResponseDTO findById(Long id) {
        ArticleStock article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", id));
        return stockMapper.toResponseDTO(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleStockResponseDTO> findByCommercant(Long idCommercant, StatutArticle statut, CategorieProduit categorie) {
        List<ArticleStock> articles;
        if (statut != null) {
            articles = articleRepository.findByIdCommercantAndStatut(idCommercant, statut);
        } else if (categorie != null) {
            articles = articleRepository.findByIdCommercantAndCategorie(idCommercant, categorie);
        } else {
            articles = articleRepository.findByIdCommercant(idCommercant);
        }
        return articles.stream().map(stockMapper::toResponseDTO).toList();
    }

    @Override
    public ArticleStockResponseDTO create(ArticleStockRequestDTO requestDTO) {
        ArticleStock entity = stockMapper.toEntity(requestDTO);

        // Mettre à jour automatiquement le statut si la DLC est déjà très proche
        ajusterStatutSelonDLC(entity);

        ArticleStock saved = articleRepository.save(entity);

        // Enregistrer le mouvement initial d'entrée en stock
        MouvementStock mvt = MouvementStock.builder()
                .article(saved)
                .type(TypeMouvement.ENTREE_STOCK)
                .quantite(saved.getQuantiteDisponible())
                .dateMouvement(LocalDateTime.now())
                .motif("Entrée en stock initiale")
                .auteur("Commerçant #" + saved.getIdCommercant())
                .build();
        mouvementRepository.save(mvt);

        return stockMapper.toResponseDTO(saved);
    }

    @Override
    public ArticleStockResponseDTO update(Long id, ArticleStockRequestDTO requestDTO) {
        ArticleStock existing = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", id));

        Double ancienneQuantite = existing.getQuantiteDisponible();
        stockMapper.updateEntityFromDTO(requestDTO, existing);
        ajusterStatutSelonDLC(existing);

        ArticleStock updated = articleRepository.save(existing);

        // Si la quantité a été modifiée manuellement, tracer un ajustement
        if (!ancienneQuantite.equals(updated.getQuantiteDisponible())) {
            double delta = updated.getQuantiteDisponible() - ancienneQuantite;
            MouvementStock mvt = MouvementStock.builder()
                    .article(updated)
                    .type(TypeMouvement.AJUSTEMENT_INVENTAIRE)
                    .quantite(delta)
                    .dateMouvement(LocalDateTime.now())
                    .motif(String.format("Ajustement inventaire : %.2f -> %.2f", ancienneQuantite, updated.getQuantiteDisponible()))
                    .auteur("Commerçant #" + updated.getIdCommercant())
                    .build();
            mouvementRepository.save(mvt);
        }

        return stockMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article de stock", id);
        }
        articleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleStockResponseDTO> getArticlesProchesPeremption(Long idCommercant, int jours) {
        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(jours);
        return articleRepository.findArticlesProchesPeremption(idCommercant, debut, fin).stream()
                .map(stockMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleStockResponseDTO> getArticlesEnSeuilCritique(Long idCommercant) {
        return articleRepository.findArticlesEnSeuilCritique(idCommercant).stream()
                .map(stockMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StockAlerteDTO getSyntheseAlertesCommercant(Long idCommercant, int jours) {
        List<ArticleStockResponseDTO> prochesPeremption = getArticlesProchesPeremption(idCommercant, jours);
        List<ArticleStockResponseDTO> seuilCritique = getArticlesEnSeuilCritique(idCommercant);

        long total = articleRepository.countByIdCommercant(idCommercant);
        long enRupture = articleRepository.countByIdCommercantAndStatut(idCommercant, StatutArticle.PERIME);

        return StockAlerteDTO.builder()
                .idCommercant(idCommercant)
                .totalArticlesEnStock(total)
                .nombreArticlesProchesPeremption(prochesPeremption.size())
                .nombreArticlesEnRupture(enRupture)
                .articlesProchesPeremption(prochesPeremption)
                .articlesEnSeuilCritique(seuilCritique)
                .build();
    }

    @Override
    public ArticleStockResponseDTO reserverStockPourOffre(Long idArticle, ReservationStockRequestDTO reservationDTO) {
        ArticleStock article = articleRepository.findById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", idArticle));

        if (article.getQuantiteDisponible() < reservationDTO.getQuantite()) {
            throw new StockInsuffisantException(idArticle, reservationDTO.getQuantite(), article.getQuantiteDisponible());
        }

        article.setQuantiteDisponible(article.getQuantiteDisponible() - reservationDTO.getQuantite());
        article.setStatut(StatutArticle.RESERVE_OFFRE);

        ArticleStock saved = articleRepository.save(article);

        MouvementStock mvt = MouvementStock.builder()
                .article(saved)
                .type(TypeMouvement.RESERVATION_OFFRE)
                .quantite(reservationDTO.getQuantite())
                .dateMouvement(LocalDateTime.now())
                .referenceExterne(reservationDTO.getReferenceExterne())
                .motif(reservationDTO.getMotif() != null ? reservationDTO.getMotif() : "Allocation à une offre anti-gaspillage")
                .auteur(reservationDTO.getAuteur() != null ? reservationDTO.getAuteur() : "ms-offres")
                .build();
        mouvementRepository.save(mvt);

        return stockMapper.toResponseDTO(saved);
    }

    @Override
    public ArticleStockResponseDTO reserverStockPourDon(Long idArticle, ReservationStockRequestDTO reservationDTO) {
        ArticleStock article = articleRepository.findById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", idArticle));

        if (article.getQuantiteDisponible() < reservationDTO.getQuantite()) {
            throw new StockInsuffisantException(idArticle, reservationDTO.getQuantite(), article.getQuantiteDisponible());
        }

        article.setQuantiteDisponible(article.getQuantiteDisponible() - reservationDTO.getQuantite());
        article.setStatut(StatutArticle.RESERVE_DON);

        ArticleStock saved = articleRepository.save(article);

        MouvementStock mvt = MouvementStock.builder()
                .article(saved)
                .type(TypeMouvement.RESERVATION_DON)
                .quantite(reservationDTO.getQuantite())
                .dateMouvement(LocalDateTime.now())
                .referenceExterne(reservationDTO.getReferenceExterne())
                .motif(reservationDTO.getMotif() != null ? reservationDTO.getMotif() : "Allocation à un don alimentaire pour association")
                .auteur(reservationDTO.getAuteur() != null ? reservationDTO.getAuteur() : "ms-dons")
                .build();
        mouvementRepository.save(mvt);

        return stockMapper.toResponseDTO(saved);
    }

    @Override
    public ArticleStockResponseDTO libererReservationStock(Long idArticle, Double quantite, String motif) {
        ArticleStock article = articleRepository.findById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", idArticle));

        if (quantite <= 0) {
            throw new BusinessException("La quantité à libérer doit être strictement positive.");
        }

        article.setQuantiteDisponible(article.getQuantiteDisponible() + quantite);
        ajusterStatutSelonDLC(article);

        ArticleStock saved = articleRepository.save(article);

        MouvementStock mvt = MouvementStock.builder()
                .article(saved)
                .type(TypeMouvement.ANNULATION_RESERVATION)
                .quantite(quantite)
                .dateMouvement(LocalDateTime.now())
                .motif(motif != null ? motif : "Libération de stock réservé")
                .auteur("System")
                .build();
        mouvementRepository.save(mvt);

        return stockMapper.toResponseDTO(saved);
    }

    @Override
    public ArticleStockResponseDTO rebuterArticlePerime(Long idArticle, String motif) {
        ArticleStock article = articleRepository.findById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException("Article de stock", idArticle));

        double quantiteRebutee = article.getQuantiteDisponible();
        article.setQuantiteDisponible(0.0);
        article.setStatut(StatutArticle.REBUTE);

        ArticleStock saved = articleRepository.save(article);

        MouvementStock mvt = MouvementStock.builder()
                .article(saved)
                .type(TypeMouvement.PERIME_REBUT)
                .quantite(quantiteRebutee)
                .dateMouvement(LocalDateTime.now())
                .motif(motif != null ? motif : "Produit périmé mis au rebut")
                .auteur("Inventaire")
                .build();
        mouvementRepository.save(mvt);

        return stockMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MouvementStockDTO> getMouvementsByArticle(Long idArticle) {
        if (!articleRepository.existsById(idArticle)) {
            throw new ResourceNotFoundException("Article de stock", idArticle);
        }
        return mouvementRepository.findByArticleIdOrderByDateMouvementDesc(idArticle).stream()
                .map(mouvementMapper::toDTO)
                .toList();
    }

    private void ajusterStatutSelonDLC(ArticleStock article) {
        if (article.getDatePeremption() == null) {
            return;
        }

        LocalDate aujourdhui = LocalDate.now();
        if (article.getDatePeremption().isBefore(aujourdhui)) {
            article.setStatut(StatutArticle.PERIME);
        } else if (article.getDatePeremption().isBefore(aujourdhui.plusDays(4))) {
            if (article.getStatut() == StatutArticle.DISPONIBLE) {
                article.setStatut(StatutArticle.PROCHE_PEREMPTION);
            }
        }
    }
}
