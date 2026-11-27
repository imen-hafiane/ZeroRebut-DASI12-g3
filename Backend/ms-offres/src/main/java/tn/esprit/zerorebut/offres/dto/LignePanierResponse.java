package tn.esprit.zerorebut.offres.dto;

public record LignePanierResponse(
        Long idLigne,
        Long idProduit,
        Integer quantite
) {
}
