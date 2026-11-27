package tn.esprit.zerorebut.stock.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock", indexes = {
        @Index(name = "idx_mvt_article", columnList = "article_id"),
        @Index(name = "idx_mvt_date", columnList = "dateMouvement")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleStock article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private TypeMouvement type;

    @Column(nullable = false)
    private Double quantite;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @Column(length = 80)
    private String referenceExterne; // ID de l'offre (MS2) ou du don (MS4)

    @Column(length = 255)
    private String motif;

    @Column(length = 80)
    private String auteur;

    @PrePersist
    public void prePersist() {
        if (this.dateMouvement == null) {
            this.dateMouvement = LocalDateTime.now();
        }
    }
}
