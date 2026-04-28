package com.example.visa.demande.model;

import java.time.LocalDateTime;
import java.util.List;

public record DemandeDetailData(
        Integer id,
        String nomComplet,
        String categorieDemande,
        String statut,
        LocalDateTime dateCreation,
        List<PieceScanItem> pieces
) {
}