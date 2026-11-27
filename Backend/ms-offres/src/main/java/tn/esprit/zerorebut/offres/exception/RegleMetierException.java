package tn.esprit.zerorebut.offres.exception;

/**
 * Levée quand une règle métier du domaine Offres n'est pas respectée
 * (transition de statut interdite, prix incohérents, quantité insuffisante...).
 * Sera traduite en HTTP 409 / 422 par le GlobalExceptionHandler.
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
