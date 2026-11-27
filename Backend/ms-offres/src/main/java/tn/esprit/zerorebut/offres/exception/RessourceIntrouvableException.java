package tn.esprit.zerorebut.offres.exception;

/** Levée quand une offre, une catégorie ou une ligne n'existe pas. Traduite en HTTP 404. */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String ressource, Long id) {
        super(ressource + " introuvable (id = " + id + ")");
    }
}
