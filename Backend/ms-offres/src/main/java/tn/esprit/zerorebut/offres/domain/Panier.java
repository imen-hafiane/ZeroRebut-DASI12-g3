package tn.esprit.zerorebut.offres.domain;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenu d'une offre : un panier composé de lignes (produits de MS1).
 * quantite = nombre de paniers identiques mis en vente par le commerçant.
 */
@Entity
@Table(name = "paniers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPanier;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    @ToString.Exclude
    private Categorie categorie;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<LignePanier> lignes = new ArrayList<>();

    // ---------- Comportements métier ----------

    /** Ajoute un produit en gardant la relation bidirectionnelle cohérente. */
    public void ajouterLigne(LignePanier ligne) {
        if (ligne.getQuantite() == null || ligne.getQuantite() <= 0) {
            throw new RegleMetierException("La quantité d'un produit doit être positive");
        }
        ligne.setPanier(this);
        lignes.add(ligne);
    }

    public void retirerLigne(LignePanier ligne) {
        lignes.remove(ligne);
        ligne.setPanier(null);
    }

    public void modifierQuantite(int nouvelleQuantite) {
        if (nouvelleQuantite <= 0) {
            throw new RegleMetierException("Le nombre de paniers doit être positif");
        }
        this.quantite = nouvelleQuantite;
    }

    public boolean estVide() {
        return lignes.isEmpty();
    }
}
