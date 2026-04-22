package com.example.visa.demande.repository;

import com.example.visa.demande.model.DemandeEditData;
import com.example.visa.demande.model.DemandeForm;
import com.example.visa.demande.model.DemandeListItem;
import com.example.visa.demande.model.OptionItem;
import com.example.visa.demande.model.PieceJustificativeItem;
import java.util.List;
import java.util.Optional;

public interface DemandeRepository {

    List<OptionItem> findSituationsFamille();

    List<OptionItem> findNationalites();

    List<PieceJustificativeItem> findPiecesCommunes();

    List<PieceJustificativeItem> findPiecesByCategorie(String categorieDemande);

    List<DemandeListItem> findDemandes();

    Integer createDemande(DemandeForm form);

    Optional<DemandeEditData> findForEdit(Integer demandeId);

    void updateDemande(Integer demandeId, DemandeForm form);
}
