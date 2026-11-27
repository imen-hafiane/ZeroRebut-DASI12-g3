package tn.esprit.zerorebut.offres.domain;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agrégat racine du MS2 : une offre anti-gaspillage publiée par un commerçant.
 * Toutes les règles de cycle de vie sont ici : le service ne fait qu'orchestrer.
 */
@Entity
@Table(name = "offres", indexes = {
        @Index(name = "idx_offre_commercant", columnList = "idCommercant"),
        @Index(name = "idx_offre_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOffre;

    /** Identifiant Keycloak (sub) du commerçant. */
    @Column(nullable = false, length = 64)
    private String idCommercant;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(length = 1000)
    private String description;

    /** BigDecimal pour les montants : pas d'erreur d'arrondi comme avec double. */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal prixInitial;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal prixReduit;

    /** Nombre de paniers encore réservables. */
    @Column(nullable = false)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Integer quantiteDisponible = 0;

    @Setter(AccessLevel.NONE)
    private LocalDateTime datePublication;

    /** Date limite de récupération : au-delà, l'offre expire. */
    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private StatutOffre statut = StatutOffre.BROUILLON;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "id_panier", nullable = false, unique = true)
    @ToString.Exclude
    private Panier panier;

    /** Verrou optimiste : deux réservations simultanées ne peuvent pas écraser la quantité. */
    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    // ---------- Comportements métier ----------

    /** BROUILLON ou SUSPENDUE -> DISPONIBLE, après vérification de la cohérence de l'offre. */
    public void publier() {
        if (statut != StatutOffre.BROUILLON && statut != StatutOffre.SUSPENDUE) {
            throw new RegleMetierException("Seule une offre en brouillon ou suspendue peut être publiée (statut actuel : " + statut + ")");
        }
        verifierCoherence();
        if (estExpiree()) {
            throw new RegleMetierException("La date d'expiration est déjà dépassée");
        }
        if (statut == StatutOffre.BROUILLON) {
            this.quantiteDisponible = panier.getQuantite();
            this.datePublication = LocalDateTime.now();
        }
        this.statut = quantiteDisponible > 0 ? StatutOffre.DISPONIBLE : StatutOffre.EPUISEE;
    }

    /** Les champs modifiables sont gérés par le service ; ici on vérifie seulement que c'est permis. */
    public void verifierModifiable() {
        if (statut.estFinal() || statut == StatutOffre.EPUISEE) {
            throw new RegleMetierException("Une offre " + statut + " ne peut plus être modifiée");
        }
    }

    /** DISPONIBLE -> SUSPENDUE (par le commerçant ou suite à un signalement MS6). */
    public void suspendre() {
        if (statut != StatutOffre.DISPONIBLE) {
            throw new RegleMetierException("Seule une offre disponible peut être suspendue (statut actuel : " + statut + ")");
        }
        this.statut = StatutOffre.SUSPENDUE;
    }

    /** Désactivation définitive par le commerçant. */
    public void desactiver() {
        if (statut.estFinal()) {
            throw new RegleMetierException("L'offre est déjà " + statut);
        }
        this.statut = StatutOffre.ANNULEE;
    }

    /** Vrai si l'offre est publiée et contient au moins `quantite` paniers. */
    public boolean verifierDisponibilite(int quantite) {
        return statut == StatutOffre.DISPONIBLE && !estExpiree() && quantiteDisponible >= quantite;
    }

    /** Appelé par MS3 lors d'une réservation : décrémente le stock de paniers. */
    public void reserver(int quantite) {
        if (quantite <= 0) {
            throw new RegleMetierException("La quantité réservée doit être positive");
        }
        if (!verifierDisponibilite(quantite)) {
            throw new RegleMetierException("Offre indisponible ou quantité insuffisante (reste " + quantiteDisponible + ")");
        }
        this.quantiteDisponible -= quantite;
        if (quantiteDisponible == 0) {
            this.statut = StatutOffre.EPUISEE;
        }
    }

    /** Appelé par MS3 lors d'une annulation : remet les paniers en vente. */
    public void liberer(int quantite) {
        if (quantite <= 0) {
            throw new RegleMetierException("La quantité libérée doit être positive");
        }
        this.quantiteDisponible = Math.min(quantiteDisponible + quantite, panier.getQuantite());
        if (statut == StatutOffre.EPUISEE && !estExpiree()) {
            this.statut = StatutOffre.DISPONIBLE;
        }
    }

    /** Appelé par la tâche planifiée : passe à EXPIREE si la date est dépassée. */
    public boolean expirerSiNecessaire() {
        if (!statut.estFinal() && estExpiree()) {
            this.statut = StatutOffre.EXPIREE;
            return true;
        }
        return false;
    }

    public boolean estExpiree() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDateTime.now());
    }

    private void verifierCoherence() {
        if (prixInitial == null || prixReduit == null
                || prixInitial.signum() <= 0 || prixReduit.signum() <= 0) {
            throw new RegleMetierException("Les prix doivent être positifs");
        }
        if (prixReduit.compareTo(prixInitial) >= 0) {
            throw new RegleMetierException("Le prix réduit doit être inférieur au prix initial");
        }
        if (panier == null || panier.estVide()) {
            throw new RegleMetierException("Le panier doit contenir au moins un produit");
        }
        if (panier.getQuantite() == null || panier.getQuantite() <= 0) {
            throw new RegleMetierException("Le nombre de paniers doit être positif");
        }
    }
}
