package com.example.visa.demande.model;

import java.time.LocalDateTime;

public record DemandeListItem(
        Integer id,
        String nomComplet,
        String categorieDemande,
        String typeDemande,
        String numeroVisa,
        String numeroCarteResident,
        String statut,
        LocalDateTime dateCreation
) {
}
