package com.example.visa.demande.controller;

import com.example.visa.demande.model.DemandeEditData;
import com.example.visa.demande.model.DemandeForm;
import com.example.visa.demande.service.DemandeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private static final String FORM_VIEW = "demandes/formulaire";
    private static final String TYPE_NOUVEAU_TITRE = "NOUVEAU_TITRE";
    private static final String TYPE_DUPLICATA_RESIDENT = "DUPLICATA_RESIDENT";
    private static final String STATUT_VISA_APPROUVE = "VISA_APPROUVE";

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("demandes", demandeService.findDemandes());
        return "demandes/liste";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        DemandeForm form = new DemandeForm();
        form.setCategorieDemande("TRAVAILLEUR");
        form.setTypeDemande(TYPE_NOUVEAU_TITRE);
        enrichFormModel(model, form, false, null);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") DemandeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!demandeService.validateBusinessRules(form, bindingResult) || bindingResult.hasErrors()) {
            enrichFormModel(model, form, false, null);
            return FORM_VIEW;
        }

        Integer demandeId = demandeService.createDemande(form);
        redirectAttributes.addFlashAttribute("successMessage", "Demande creee avec statut DOSSIER_CREE (ID: " + demandeId + ")");
        return "redirect:/demandes";
    }

    @GetMapping("/{id}/modifier")
    public String edit(@PathVariable("id") Integer demandeId, Model model, RedirectAttributes redirectAttributes) {
        DemandeEditData editData = demandeService.findForEdit(demandeId).orElse(null);
        if (editData == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Demande introuvable");
            return "redirect:/demandes";
        }

        enrichFormModel(model, editData.form(), true, demandeId);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") Integer demandeId,
            @Valid @ModelAttribute("form") DemandeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!demandeService.validateBusinessRules(form, bindingResult) || bindingResult.hasErrors()) {
            enrichFormModel(model, form, true, demandeId);
            return FORM_VIEW;
        }

        demandeService.updateDemande(demandeId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Demande modifiee avec succes");
        return "redirect:/demandes";
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable("id") Integer demandeId, RedirectAttributes redirectAttributes) {
        demandeService.updateStatut(demandeId, STATUT_VISA_APPROUVE);
        redirectAttributes.addFlashAttribute("successMessage", "Demande validee avec succes");
        return "redirect:/demandes";
    }

    private void enrichFormModel(Model model, DemandeForm form, boolean editMode, Integer demandeId) {
        model.addAttribute("form", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("demandeId", demandeId);
        model.addAttribute("typesDemandes", List.of(TYPE_NOUVEAU_TITRE, TYPE_DUPLICATA_RESIDENT));
        model.addAttribute("categories", List.of("TRAVAILLEUR", "INVESTISSEUR"));
        model.addAttribute("situationsFamille", demandeService.findSituationsFamille());
        model.addAttribute("nationalites", demandeService.findNationalites());
        model.addAttribute("piecesCommunes", demandeService.findPiecesCommunes());
        model.addAttribute("piecesTravailleur", demandeService.findPiecesByCategorie("TRAVAILLEUR"));
        model.addAttribute("piecesInvestisseur", demandeService.findPiecesByCategorie("INVESTISSEUR"));
    }
}
