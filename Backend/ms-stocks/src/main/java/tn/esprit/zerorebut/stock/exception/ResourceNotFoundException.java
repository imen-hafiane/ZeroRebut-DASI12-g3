package tn.esprit.zerorebut.stock.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String ressource, Long id) {
        super(ressource + " avec l'identifiant " + id + " n'existe pas.");
    }
}
