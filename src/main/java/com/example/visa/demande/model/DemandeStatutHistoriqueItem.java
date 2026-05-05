package com.example.visa.demande.model;

import java.time.LocalDateTime;

public record DemandeStatutHistoriqueItem(
        Integer id,
        String statut,
        LocalDateTime date,
        String note
) {
}
