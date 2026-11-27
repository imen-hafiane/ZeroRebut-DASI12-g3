package tn.esprit.zerorebut.offres.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.zerorebut.offres.domain.Offre;
import tn.esprit.zerorebut.offres.domain.StatutOffre;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long> {

    /**
     * Recherche multicritère : chaque filtre est ignoré s'il vaut null.
     * GET /api/offres?statut=&categorie=&prixMax=&commercant=&page=&size=
     */
    @EntityGraph(attributePaths = {"panier", "panier.categorie"})
    @Query("""
            select o from Offre o
            where (:statut is null or o.statut = :statut)
              and (:idCategorie is null or o.panier.categorie.idCategorie = :idCategorie)
              and (:prixMax is null or o.prixReduit <= :prixMax)
              and (:idCommercant is null or o.idCommercant = :idCommercant)
            """)
    Page<Offre> rechercher(@Param("statut") StatutOffre statut,
                           @Param("idCategorie") Long idCategorie,
                           @Param("prixMax") BigDecimal prixMax,
                           @Param("idCommercant") String idCommercant,
                           Pageable pageable);

    /** Utilisé par la tâche planifiée d'expiration. */
    List<Offre> findByStatutInAndDateExpirationBefore(Collection<StatutOffre> statuts, LocalDateTime date);

    /** Empêche de supprimer une catégorie encore utilisée. */
    boolean existsByPanierCategorieIdCategorie(Long idCategorie);
}
