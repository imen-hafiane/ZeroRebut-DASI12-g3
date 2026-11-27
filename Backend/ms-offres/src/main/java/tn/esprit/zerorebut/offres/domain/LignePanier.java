package tn.esprit.zerorebut.offres.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Un produit (géré par MS1 · Stocks) et sa quantité dans un panier.
 * idProduit est une simple référence vers MS1 : pas de clé étrangère entre bases.
 */
@Entity
@Table(name = "lignes_panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LignePanier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigne;

    /** Identifiant de l'article dans MS1 (ms-stocks). */
    @Column(nullable = false)
    private Long idProduit;

    @Column(nullable = false)
    private Integer quantite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_panier", nullable = false)
    @ToString.Exclude
    private Panier panier;
}
