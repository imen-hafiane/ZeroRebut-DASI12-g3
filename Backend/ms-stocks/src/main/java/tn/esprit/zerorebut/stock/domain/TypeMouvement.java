package tn.esprit.zerorebut.stock.domain;

/**
 * Types de mouvements pour la traçabilité complète des stocks et du gaspillage évité.
 */
public enum TypeMouvement {
    ENTREE_STOCK,
    SORTIE_VENTE,
    RESERVATION_OFFRE,
    RESERVATION_DON,
    ANNULATION_RESERVATION,
    PERIME_REBUT,
    AJUSTEMENT_INVENTAIRE
}
