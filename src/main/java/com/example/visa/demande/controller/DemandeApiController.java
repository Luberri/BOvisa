package com.example.visa.demande.controller;

import com.example.visa.demande.model.DemandeSearchResult;
import com.example.visa.demande.service.DemandeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes")
public class DemandeApiController {

    private final DemandeService demandeService;

    public DemandeApiController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @GetMapping("/search")
    public List<DemandeSearchResult> search(
            @RequestParam("demandeId") Integer demandeId,
            @RequestParam("passeportId") Integer passeportId
    ) {
        return demandeService.findDemandesForPasseport(demandeId, passeportId);
    }
}
