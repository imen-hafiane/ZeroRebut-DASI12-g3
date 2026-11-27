package tn.esprit.zerorebut.stock.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles_stock", indexes = {
        @Index(name = "idx_commercant", columnList = "idCommercant"),
        @Index(name = "idx_dlc", columnList = "datePeremption"),
        @Index(name = "idx_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 50)
    private String codeBarre;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CategorieProduit categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UniteMesure unite;

    @Column(nullable = false)
    private Double quantiteDisponible;

    @Column(nullable = false)
    private Double quantiteAlerte;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixOriginal;

    private LocalDate dateFabrication;

    @Column(nullable = false)
    private LocalDate datePeremption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutArticle statut;

    @Column(nullable = false)
    private Long idCommercant;

    @Column(length = 100)
    private String nomMagasin;

    @Column(length = 50)
    private String rayonStockage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateMiseAJour;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MouvementStock> mouvements = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateMiseAJour = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutArticle.DISPONIBLE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }

    public void ajouterMouvement(MouvementStock mouvement) {
        this.mouvements.add(mouvement);
        mouvement.setArticle(this);
    }
}
