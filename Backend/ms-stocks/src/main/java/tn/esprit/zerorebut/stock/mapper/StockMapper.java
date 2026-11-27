package tn.esprit.zerorebut.stock.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.zerorebut.stock.domain.ArticleStock;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.dto.ArticleStockRequestDTO;
import tn.esprit.zerorebut.stock.dto.ArticleStockResponseDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class StockMapper {

    public ArticleStock toEntity(ArticleStockRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return ArticleStock.builder()
                .nom(dto.getNom())
                .codeBarre(dto.getCodeBarre())
                .description(dto.getDescription())
                .categorie(dto.getCategorie())
                .unite(dto.getUnite())
                .quantiteDisponible(dto.getQuantiteDisponible())
                .quantiteAlerte(dto.getQuantiteAlerte())
                .prixOriginal(dto.getPrixOriginal())
                .dateFabrication(dto.getDateFabrication())
                .datePeremption(dto.getDatePeremption())
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutArticle.DISPONIBLE)
                .idCommercant(dto.getIdCommercant())
                .nomMagasin(dto.getNomMagasin())
                .rayonStockage(dto.getRayonStockage())
                .build();
    }

    public ArticleStockResponseDTO toResponseDTO(ArticleStock entity) {
        if (entity == null) {
            return null;
        }

        LocalDate aujourdhui = LocalDate.now();
        long joursRestants = entity.getDatePeremption() != null
                ? ChronoUnit.DAYS.between(aujourdhui, entity.getDatePeremption())
                : 0;

        boolean prochePeremption = joursRestants >= 0 && joursRestants <= 3;
        boolean seuilCritique = entity.getQuantiteDisponible() != null
                && entity.getQuantiteAlerte() != null
                && entity.getQuantiteDisponible() <= entity.getQuantiteAlerte();

        return ArticleStockResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .codeBarre(entity.getCodeBarre())
                .description(entity.getDescription())
                .categorie(entity.getCategorie())
                .unite(entity.getUnite())
                .quantiteDisponible(entity.getQuantiteDisponible())
                .quantiteAlerte(entity.getQuantiteAlerte())
                .prixOriginal(entity.getPrixOriginal())
                .dateFabrication(entity.getDateFabrication())
                .datePeremption(entity.getDatePeremption())
                .statut(entity.getStatut())
                .idCommercant(entity.getIdCommercant())
                .nomMagasin(entity.getNomMagasin())
                .rayonStockage(entity.getRayonStockage())
                .joursRestantsAvantPeremption(joursRestants)
                .estEnAlerteQuantite(seuilCritique)
                .estProchePeremption(prochePeremption)
                .dateCreation(entity.getDateCreation())
                .dateMiseAJour(entity.getDateMiseAJour())
                .build();
    }

    public void updateEntityFromDTO(ArticleStockRequestDTO dto, ArticleStock entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setNom(dto.getNom());
        entity.setCodeBarre(dto.getCodeBarre());
        entity.setDescription(dto.getDescription());
        entity.setCategorie(dto.getCategorie());
        entity.setUnite(dto.getUnite());
        entity.setQuantiteDisponible(dto.getQuantiteDisponible());
        entity.setQuantiteAlerte(dto.getQuantiteAlerte());
        entity.setPrixOriginal(dto.getPrixOriginal());
        entity.setDateFabrication(dto.getDateFabrication());
        entity.setDatePeremption(dto.getDatePeremption());
        if (dto.getStatut() != null) {
            entity.setStatut(dto.getStatut());
        }
        entity.setIdCommercant(dto.getIdCommercant());
        entity.setNomMagasin(dto.getNomMagasin());
        entity.setRayonStockage(dto.getRayonStockage());
    }
}
