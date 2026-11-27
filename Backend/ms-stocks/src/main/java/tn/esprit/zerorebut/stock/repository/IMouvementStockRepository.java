package tn.esprit.zerorebut.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.zerorebut.stock.domain.MouvementStock;

import java.util.List;

@Repository
public interface IMouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    List<MouvementStock> findByArticleIdOrderByDateMouvementDesc(Long articleId);

    List<MouvementStock> findByReferenceExterne(String referenceExterne);
}
