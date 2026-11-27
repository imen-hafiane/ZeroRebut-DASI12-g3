package tn.esprit.zerorebut.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.zerorebut.stock.domain.ArticleStock;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IArticleStockRepository extends JpaRepository<ArticleStock, Long> {

    List<ArticleStock> findByIdCommercant(Long idCommercant);

    List<ArticleStock> findByIdCommercantAndStatut(Long idCommercant, StatutArticle statut);

    List<ArticleStock> findByIdCommercantAndCategorie(Long idCommercant, CategorieProduit categorie);

    Optional<ArticleStock> findByCodeBarreAndIdCommercant(String codeBarre, Long idCommercant);

    /**
     * Recherche les articles d'un commerçant dont la DLC est comprise entre deux dates
     * et dont le statut permet encore d'en faire une offre anti-gaspi ou un don.
     */
    @Query("SELECT a FROM ArticleStock a WHERE a.idCommercant = :idCommercant " +
           "AND a.datePeremption BETWEEN :debut AND :fin " +
           "AND a.quantiteDisponible > 0 " +
           "AND a.statut IN ('DISPONIBLE', 'PROCHE_PEREMPTION') " +
           "ORDER BY a.datePeremption ASC")
    List<ArticleStock> findArticlesProchesPeremption(
            @Param("idCommercant") Long idCommercant,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    /**
     * Recherche globale de tous les articles du système proches de la péremption (pour alertes globales / Actuator)
     */
    @Query("SELECT a FROM ArticleStock a WHERE a.datePeremption BETWEEN :debut AND :fin " +
           "AND a.quantiteDisponible > 0 AND a.statut IN ('DISPONIBLE', 'PROCHE_PEREMPTION')")
    List<ArticleStock> findAllProchesPeremption(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    /**
     * Recherche les articles d'un commerçant dont le stock est inférieur ou égal au seuil critique
     */
    @Query("SELECT a FROM ArticleStock a WHERE a.idCommercant = :idCommercant " +
           "AND a.quantiteDisponible <= a.quantiteAlerte " +
           "AND a.statut NOT IN ('PERIME', 'REBUTE')")
    List<ArticleStock> findArticlesEnSeuilCritique(@Param("idCommercant") Long idCommercant);

    long countByIdCommercant(Long idCommercant);

    long countByIdCommercantAndStatut(Long idCommercant, StatutArticle statut);
}
