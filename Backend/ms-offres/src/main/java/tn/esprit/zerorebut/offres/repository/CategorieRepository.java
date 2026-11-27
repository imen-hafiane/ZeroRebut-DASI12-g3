package tn.esprit.zerorebut.offres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.zerorebut.offres.domain.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    boolean existsByNomIgnoreCase(String nom);
}
