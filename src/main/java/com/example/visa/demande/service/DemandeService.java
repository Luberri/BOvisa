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

    public void updateStatut(Integer demandeId, String statut) {
        demandeRepository.updateStatut(demandeId, statut);
    }

    public Optional<DemandeEditData> findForEdit(Integer demandeId) {
        return demandeRepository.findForEdit(demandeId);
    }

    public boolean validateBusinessRules(DemandeForm form, BindingResult bindingResult) {
        String typeDemande = form.getTypeDemande();
        if (typeDemande != null && !("NOUVEAU_TITRE".equals(typeDemande) || "DUPLICATA_RESIDENT".equals(typeDemande))) {
            bindingResult.rejectValue(
                    "typeDemande",
                    "typeDemande.invalid",
                    "Le type de demande doit etre NOUVEAU_TITRE ou DUPLICATA_RESIDENT"
            );
        }

        boolean avecDonneesAnterieures = Boolean.TRUE.equals(form.getAvecDonneesAnterieures());
        boolean duplicataSansDonneesAnterieures = "DUPLICATA_RESIDENT".equals(typeDemande) && !avecDonneesAnterieures;

        if (avecDonneesAnterieures) {
            if (!"DUPLICATA_RESIDENT".equals(typeDemande)) {
                bindingResult.rejectValue(
                        "avecDonneesAnterieures",
                        "avecDonneesAnterieures.invalid",
                        "Les donnees anterieures sont reservees au duplicata resident"
                );
            }

            if (isBlank(form.getNumeroCarteResident())) {
                bindingResult.rejectValue(
                        "numeroCarteResident",
                        "numeroCarteResident.required",
                        "Le numero de carte resident est obligatoire pour un duplicata avec donnees anterieures"
                );
            } else if (!demandeRepository.existsCarteResident(form.getNumeroCarteResident())) {
                bindingResult.rejectValue(
                        "numeroCarteResident",
                        "numeroCarteResident.invalid",
                        "Le numero de carte resident est invalide"
                );
            }
        } else {
            requireField(bindingResult, form.getNom(), "nom", "Le nom est obligatoire");
            requireField(bindingResult, form.getSituationFamilleId(), "situationFamilleId", "La situation de famille est obligatoire");
            requireField(bindingResult, form.getNationaliteId(), "nationaliteId", "La nationalite est obligatoire");
            requireField(bindingResult, form.getAdresse(), "adresse", "L'adresse a Madagascar est obligatoire");
            requireField(bindingResult, form.getNumeroPasseport(), "numeroPasseport", "Le numero du passeport est obligatoire");
            requireField(bindingResult, form.getCategorieDemande(), "categorieDemande", "La categorie est obligatoire");
            requireField(bindingResult, form.getReferenceVisa(), "referenceVisa", "La reference du visa est obligatoire");
            if (!("NOUVEAU_TITRE".equals(typeDemande) || duplicataSansDonneesAnterieures)) {
                requireField(bindingResult, form.getNumeroVisa(), "numeroVisa", "Le numero du visa est obligatoire");
            }
            requireField(bindingResult, form.getDateEntreeMada(), "dateEntreeMada", "La date d'entree a Madagascar est obligatoire");
            requireField(bindingResult, form.getLieuEntreeMada(), "lieuEntreeMada", "Le lieu d'entree a Madagascar est obligatoire");
            requireField(bindingResult, form.getDateExpirationVisa(), "dateExpirationVisa", "La date d'expiration du visa est obligatoire");

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
        }

        return !bindingResult.hasErrors();
    }

    private void requireField(BindingResult bindingResult, Object value, String fieldName, String message) {
        if (value == null) {
            bindingResult.rejectValue(fieldName, fieldName + ".required", message);
            return;
        }

        if (value instanceof String text && text.isBlank()) {
            bindingResult.rejectValue(fieldName, fieldName + ".required", message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
