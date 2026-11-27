package tn.esprit.zerorebut.offres.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.zerorebut.offres.domain.StatutOffre;
import tn.esprit.zerorebut.offres.dto.*;
import tn.esprit.zerorebut.offres.exception.RegleMetierException;
import tn.esprit.zerorebut.offres.exception.RessourceIntrouvableException;
import tn.esprit.zerorebut.offres.service.OffreService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests de la couche HTTP : codes de retour, validation et format des erreurs. */
@WebMvcTest(OffreController.class)
class OffreControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OffreService offreService;

    private OffreRequest requete(String prixInitial, String prixReduit) {
        return new OffreRequest("commercant-001", "Panier surprise", null,
                new BigDecimal(prixInitial), new BigDecimal(prixReduit), LocalDateTime.now().plusDays(1),
                new PanierRequest("Panier", null, 5, 1L, List.of(new LignePanierRequest(10L, 3))));
    }

    private OffreResponse reponse() {
        return new OffreResponse(1L, "commercant-001", "Panier surprise", null,
                new BigDecimal("12"), new BigDecimal("4"), 67, 0, null,
                LocalDateTime.now().plusDays(1), StatutOffre.BROUILLON, null);
    }

    @Test
    @DisplayName("POST /api/offres valide -> 201 + Location")
    void creer_201() throws Exception {
        when(offreService.creer(any())).thenReturn(reponse());

        mockMvc.perform(post("/api/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requete("12", "4"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/offres/1"))
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
    }

    @Test
    @DisplayName("POST /api/offres avec prix réduit > prix initial -> 400")
    void creer_400() throws Exception {
        mockMvc.perform(post("/api/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requete("5", "8"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.prixCoherent").exists());
    }

    @Test
    @DisplayName("GET /api/offres/999 -> 404")
    void obtenir_404() throws Exception {
        when(offreService.obtenir(999L)).thenThrow(new RessourceIntrouvableException("Offre", 999L));

        mockMvc.perform(get("/api/offres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"));
    }

    @Test
    @DisplayName("PUT /api/offres/1/reserver avec quantité insuffisante -> 409")
    void reserver_409() throws Exception {
        when(offreService.reserver(eq(1L), anyInt()))
                .thenThrow(new RegleMetierException("Offre indisponible ou quantité insuffisante"));

        mockMvc.perform(put("/api/offres/1/reserver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantite\": 10}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/offres/1/disponibilite?quantite=0 -> 400")
    void disponibilite_400() throws Exception {
        mockMvc.perform(get("/api/offres/1/disponibilite").param("quantite", "0"))
                .andExpect(status().isBadRequest());
    }
}
