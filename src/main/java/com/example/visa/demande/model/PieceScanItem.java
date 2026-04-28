package com.example.visa.demande.model;

import java.time.LocalDateTime;

public record PieceScanItem(
        Integer pieceId,
        String libelle,
        boolean rattachee,
        boolean scannee,
        LocalDateTime dateModification,
        String motif
) {
}