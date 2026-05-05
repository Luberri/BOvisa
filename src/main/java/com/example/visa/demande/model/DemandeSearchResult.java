package com.example.visa.demande.model;

import java.time.LocalDateTime;
import java.util.List;

public record DemandeSearchResult(
        Integer id,
        Integer passeportId,
        String titre,
        String type,
        String statutActuel,
        LocalDateTime dateCreation,
        String demandeur,
        List<DemandeStatutHistoriqueItem> historique
) {
}
