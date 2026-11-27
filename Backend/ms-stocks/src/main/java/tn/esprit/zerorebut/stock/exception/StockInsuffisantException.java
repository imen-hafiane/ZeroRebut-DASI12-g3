package tn.esprit.zerorebut.stock.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }

    public StockInsuffisantException(Long idArticle, Double demande, Double disponible) {
        super(String.format("Stock insuffisant pour l'article #%d : demandé %.2f, disponible %.2f",
                idArticle, demande, disponible));
    }
}
