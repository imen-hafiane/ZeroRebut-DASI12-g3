package tn.esprit.zerorebut.stock.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.zerorebut.stock.domain.MouvementStock;
import tn.esprit.zerorebut.stock.dto.MouvementStockDTO;

@Component
public class MouvementMapper {

    public MouvementStockDTO toDTO(MouvementStock entity) {
        if (entity == null) {
            return null;
        }

        return MouvementStockDTO.builder()
                .id(entity.getId())
                .articleId(entity.getArticle() != null ? entity.getArticle().getId() : null)
                .articleNom(entity.getArticle() != null ? entity.getArticle().getNom() : null)
                .type(entity.getType())
                .quantite(entity.getQuantite())
                .dateMouvement(entity.getDateMouvement())
                .referenceExterne(entity.getReferenceExterne())
                .motif(entity.getMotif())
                .auteur(entity.getAuteur())
                .build();
    }
}
