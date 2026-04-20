package com.example.visa.demande.service;

import com.example.visa.demande.model.DemandeEditData;
import com.example.visa.demande.model.DemandeForm;
import com.example.visa.demande.model.DemandeListItem;
import com.example.visa.demande.model.OptionItem;
import com.example.visa.demande.model.PieceJustificativeItem;
import com.example.visa.demande.repository.DemandeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
public class DemandeService {

    private final DemandeRepository demandeRepository;

    public DemandeService(DemandeRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
    }

    public List<OptionItem> findSituationsFamille() {
        return demandeRepository.findSituationsFamille();
    }

    public List<OptionItem> findNationalites() {
        return demandeRepository.findNationalites();
    }

    public List<PieceJustificativeItem> findPiecesCommunes() {
        return demandeRepository.findPiecesCommunes();
    }

    public List<PieceJustificativeItem> findPiecesByCategorie(String categorieDemande) {
        return demandeRepository.findPiecesByCategorie(categorieDemande);
    }

    public List<DemandeListItem> findDemandes() {
        return demandeRepository.findDemandes();
    }

    public Optional<DemandeEditData> findForEdit(Integer demandeId) {
        return demandeRepository.findForEdit(demandeId);
    }

    public boolean validateBusinessRules(DemandeForm form, BindingResult bindingResult) {
        if (form.getDateExpirationVisa() != null && !LocalDate.now().isBefore(form.getDateExpirationVisa())) {
            bindingResult.rejectValue(
                    "dateExpirationVisa",
                    "dateExpirationVisa.invalid",
                    "La demande doit etre faite avant la date d'expiration du visa"
            );
        }

        String categorie = form.getCategorieDemande();
        if (categorie != null && !("TRAVAILLEUR".equals(categorie) || "INVESTISSEUR".equals(categorie))) {
            bindingResult.rejectValue(
                    "categorieDemande",
                    "categorieDemande.invalid",
                    "La categorie doit etre TRAVAILLEUR ou INVESTISSEUR"
            );
        }

        return !bindingResult.hasErrors();
    }

    @Transactional
    public Integer createDemande(DemandeForm form) {
        return demandeRepository.createDemande(form);
    }

    @Transactional
    public void updateDemande(Integer demandeId, DemandeForm form) {
        demandeRepository.updateDemande(demandeId, form);
    }
}
