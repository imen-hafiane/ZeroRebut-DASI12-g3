package tn.esprit.zerorebut.offres.web;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;
import tn.esprit.zerorebut.offres.exception.RessourceIntrouvableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les exceptions en réponses JSON standard (RFC 9457 « Problem Details »).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ProblemDetail introuvable(RessourceIntrouvableException ex) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    public ProblemDetail regleMetier(RegleMetierException ex) {
        return probleme(HttpStatus.CONFLICT, "Règle métier non respectée", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erreurs.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> erreurs.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        ProblemDetail pd = probleme(HttpStatus.BAD_REQUEST, "Données invalides", "Un ou plusieurs champs sont invalides");
        pd.setProperty("erreurs", erreurs);
        return pd;
    }

    /** Contraintes sur les paramètres d'URL (ex. quantite=@Positive). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail parametres(HandlerMethodValidationException ex) {
        String detail = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> r.getMethodParameter().getParameterName() + " : " + e.getDefaultMessage()))
                .collect(java.util.stream.Collectors.joining(", "));
        return probleme(HttpStatus.BAD_REQUEST, "Paramètre invalide", detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail contrainte(ConstraintViolationException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Paramètre invalide", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonIllisible(HttpMessageNotReadableException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Corps de requête illisible",
                "JSON invalide ou valeur mal formatée (dates au format 2026-12-31T20:00:00)");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail mauvaisType(MethodArgumentTypeMismatchException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Paramètre invalide",
                "Valeur '" + ex.getValue() + "' invalide pour le paramètre '" + ex.getName() + "'");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrence(ObjectOptimisticLockingFailureException ex) {
        return probleme(HttpStatus.CONFLICT, "Modification concurrente",
                "L'offre a été modifiée en même temps par une autre requête, veuillez réessayer");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrite(DataIntegrityViolationException ex) {
        return probleme(HttpStatus.CONFLICT, "Contrainte de données", "L'opération viole une contrainte de la base");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail routeInconnue(NoResourceFoundException ex) {
        return probleme(HttpStatus.NOT_FOUND, "Route inconnue", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail methode(HttpRequestMethodNotSupportedException ex) {
        return probleme(HttpStatus.METHOD_NOT_ALLOWED, "Méthode non autorisée", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inattendue(Exception ex) {
        log.error("Erreur inattendue", ex);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", "Une erreur inattendue est survenue");
    }

    private ProblemDetail probleme(HttpStatus status, String titre, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(titre);
        pd.setProperty("timestamp", LocalDateTime.now());
        return pd;
    }
}
