package tn.esprit.zerorebut.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Synthèse des alertes de stock (DLC imminente, ruptures) pour un commerçant")
public class StockAlerteDTO {

    private Long idCommercant;
    private long totalArticlesEnStock;
    private long nombreArticlesProchesPeremption;
    private long nombreArticlesEnRupture;
    private List<ArticleStockResponseDTO> articlesProchesPeremption;
    private List<ArticleStockResponseDTO> articlesEnSeuilCritique;
}
