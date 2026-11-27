package tn.esprit.zerorebut.offres.domain;

/**
 * Cycle de vie d'une offre anti-gaspillage.
 *
 * BROUILLON --publier--> DISPONIBLE --(quantité = 0)--> EPUISEE
 *                          |  ^                          |
 *                 suspendre|  |publier                   | liberer
 *                          v  |                          v
 *                        SUSPENDUE                   DISPONIBLE
 *
 * Toute offre non finale peut passer à EXPIREE (date dépassée) ou ANNULEE (désactivation).
 */
public enum StatutOffre {
    BROUILLON,
    DISPONIBLE,
    SUSPENDUE,
    EPUISEE,
    EXPIREE,
    ANNULEE;

    /** EXPIREE et ANNULEE sont des états finaux : plus aucune action possible. */
    public boolean estFinal() {
        return this == EXPIREE || this == ANNULEE;
    }
}
