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

    public Optional<DemandeEditData> findForEdit(Integer demandeId) {
        return demandeRepository.findForEdit(demandeId);
    }

    public Optional<DemandeDetailData> findDetail(Integer demandeId) {
        return demandeRepository.findDetail(demandeId);
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
