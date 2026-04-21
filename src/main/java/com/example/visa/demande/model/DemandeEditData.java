package com.example.visa.demande.model;

import java.util.Set;

public record DemandeEditData(
        Integer demandeurId,
        Integer visaId,
        DemandeForm form,
        Set<Integer> pieceIds
) {
}
