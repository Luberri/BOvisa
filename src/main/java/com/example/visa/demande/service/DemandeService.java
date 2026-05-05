package com.example.visa.demande.service;

import com.example.visa.demande.model.DemandeEditData;
import com.example.visa.demande.model.DemandeDetailData;
import com.example.visa.demande.model.PieceScanItem;
import com.example.visa.demande.model.DemandeForm;
import com.example.visa.demande.model.DemandeListItem;
import com.example.visa.demande.model.OptionItem;
import com.example.visa.demande.model.PieceJustificativeItem;
import com.example.visa.demande.repository.DemandeRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

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

    public Optional<DemandeDetailData> findDetail(Integer demandeId) {
        return demandeRepository.findDetail(demandeId);
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

    @Transactional
    public void scanPiece(Integer demandeId, Integer pieceId, String motif, MultipartFile[] fichiers) {
        DemandeDetailData detailData = demandeRepository.findDetail(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        // Verify piece is attached
        PieceScanItem pieceItem = detailData.pieces().stream()
                .filter(p -> p.pieceId().equals(pieceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Piece introuvable pour cette demande"));

        // Determine required count from piece libelle (e.g., "02 photos d'identité")
        int requiredCount = 1;
        try {
            String lib = pieceItem.libelle();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\s*0*([0-9]+)").matcher(lib);
            if (m.find()) {
                requiredCount = Integer.parseInt(m.group(1));
                if (requiredCount < 1) requiredCount = 1;
            }
        } catch (Exception ignored) {
            requiredCount = 1;
        }

        int provided = 0;
        if (fichiers != null) {
            for (MultipartFile mf : fichiers) {
                if (mf != null && !mf.isEmpty()) provided++;
            }
        }

        if (provided < requiredCount) {
            throw new IllegalArgumentException("Veuillez fournir au moins " + requiredCount + " fichier(s) pour la pièce : " + pieceItem.libelle());
        }

        String motifNettoye = motif == null || motif.isBlank() ? null : motif.trim();

        // Persist each uploaded file as a historique_remis entry
        if (fichiers != null) {
            for (MultipartFile mf : fichiers) {
                if (mf == null || mf.isEmpty()) continue;
                String fichierNom = mf.getOriginalFilename();
                String fichierType = mf.getContentType();
                byte[] fichierContenu;
                try {
                    fichierContenu = mf.getBytes();
                } catch (IOException ex) {
                    throw new IllegalStateException("Impossible de lire le fichier scanne", ex);
                }
                demandeRepository.recordPieceScan(demandeId, pieceId, motifNettoye, fichierNom, fichierType, fichierContenu);
            }
        }

        if (demandeRepository.areAllPiecesScanned(demandeId)) {
            demandeRepository.markDemandeScanComplete(demandeId);
        }
    }
}
