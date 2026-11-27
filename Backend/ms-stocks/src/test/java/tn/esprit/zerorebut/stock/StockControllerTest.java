package tn.esprit.zerorebut.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.zerorebut.stock.domain.CategorieProduit;
import tn.esprit.zerorebut.stock.domain.StatutArticle;
import tn.esprit.zerorebut.stock.domain.UniteMesure;
import tn.esprit.zerorebut.stock.dto.ArticleStockRequestDTO;
import tn.esprit.zerorebut.stock.dto.ArticleStockResponseDTO;
import tn.esprit.zerorebut.stock.dto.ReservationStockRequestDTO;
import tn.esprit.zerorebut.stock.exception.ResourceNotFoundException;
import tn.esprit.zerorebut.stock.service.IStockService;
import tn.esprit.zerorebut.stock.web.controller.StockController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IStockService stockService;

    @Test
    @DisplayName("GET /api/v1/stocks -> 200 OK")
    void testFindAll() throws Exception {
        ArticleStockResponseDTO dto = ArticleStockResponseDTO.builder()
                .id(1L)
                .nom("Yaourts nature")
                .categorie(CategorieProduit.PRODUITS_LAITIERS)
                .quantiteDisponible(20.0)
                .statut(StatutArticle.DISPONIBLE)
                .build();

        when(stockService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Yaourts nature"));
    }

    @Test
    @DisplayName("GET /api/v1/stocks/{id} inexistant -> 404 Not Found")
    void testFindByIdNotFound() throws Exception {
        when(stockService.findById(999L)).thenThrow(new ResourceNotFoundException("Article de stock", 999L));

        mockMvc.perform(get("/api/v1/stocks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("POST /api/v1/stocks valide -> 201 Created")
    void testCreateArticleSuccess() throws Exception {
        ArticleStockRequestDTO request = ArticleStockRequestDTO.builder()
                .nom("Salade bio")
                .categorie(CategorieProduit.FRUITS_ET_LEGUMES)
                .unite(UniteMesure.PIECE)
                .quantiteDisponible(10.0)
                .quantiteAlerte(2.0)
                .prixOriginal(BigDecimal.valueOf(2.50))
                .datePeremption(LocalDate.now().plusDays(3))
                .idCommercant(101L)
                .nomMagasin("Primeur Bio")
                .build();

        ArticleStockResponseDTO response = ArticleStockResponseDTO.builder()
                .id(5L)
                .nom("Salade bio")
                .statut(StatutArticle.DISPONIBLE)
                .build();

        when(stockService.create(any(ArticleStockRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nom").value("Salade bio"));
    }

    @Test
    @DisplayName("POST /api/v1/stocks invalide (nom vide, prix négatif) -> 400 Bad Request")
    void testCreateArticleValidationErrors() throws Exception {
        ArticleStockRequestDTO request = ArticleStockRequestDTO.builder()
                .nom("") // invalide
                .prixOriginal(BigDecimal.valueOf(-5.0)) // invalide
                .build();

        mockMvc.perform(post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.prixOriginal").exists());
    }

    @Test
    @DisplayName("POST /api/v1/stocks/{id}/reserver-offre -> 200 OK")
    void testReserverOffre() throws Exception {
        ReservationStockRequestDTO resRequest = ReservationStockRequestDTO.builder()
                .quantite(3.0)
                .referenceExterne("OFFRE-42")
                .motif("Panier surprise")
                .build();

        ArticleStockResponseDTO response = ArticleStockResponseDTO.builder()
                .id(1L)
                .nom("Salade bio")
                .quantiteDisponible(7.0)
                .statut(StatutArticle.RESERVE_OFFRE)
                .build();

        when(stockService.reserverStockPourOffre(eq(1L), any(ReservationStockRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/stocks/1/reserver-offre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteDisponible").value(7.0))
                .andExpect(jsonPath("$.statut").value("RESERVE_OFFRE"));
    }
}
