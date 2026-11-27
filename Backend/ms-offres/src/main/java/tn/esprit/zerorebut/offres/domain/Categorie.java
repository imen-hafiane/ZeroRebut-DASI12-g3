package tn.esprit.zerorebut.offres.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catégorie d'un panier (Boulangerie, Fruits et légumes, Plats préparés...).
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategorie;

    @Column(nullable = false, unique = true, length = 80)
    private String nom;

    @Column(length = 255)
    private String description;
}
