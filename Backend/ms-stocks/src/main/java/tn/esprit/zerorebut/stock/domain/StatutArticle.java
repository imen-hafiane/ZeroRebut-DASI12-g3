package tn.esprit.zerorebut.stock.domain;

/**
 * Statuts possibles d'un article en stock dans la démarche anti-gaspillage ZeroRebut.
 */
public enum StatutArticle {
    DISPONIBLE,          // En rayon / réserve standard
    PROCHE_PEREMPTION,   // Seuil de DLC critique atteint (éligible pour paniers anti-gaspi ou dons)
    RESERVE_OFFRE,       // Alloué à une offre / panier anti-gaspi (MS2-Offres)
    RESERVE_DON,         // Alloué à un don pour association (MS4-Dons)
    VENDU,               // Vendu
    DONNE,               // Donné à une association
    PERIME,              // DLC dépassée
    REBUTE               // Impropre à la consommation / jeté
}
