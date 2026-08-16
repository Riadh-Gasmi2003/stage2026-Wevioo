package tn.econstruction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.service.BuildingPermitService;

@RestController
@RequestMapping("/api/building-permits")
@RequiredArgsConstructor
public class BuildingPermitController {

    private final BuildingPermitService buildingPermitService;

    // Téléchargement du permis PDF (RM-015) — l'historique doit permettre au citoyen
    // de retélécharger le document dans l'une ou l'autre langue, indépendamment de
    // sa préférence actuelle (les deux versions sont générées dès l'émission).
    @GetMapping("/application/{applicationId}/pdf")
    public ResponseEntity<byte[]> getPermitPdf(
            @PathVariable Long applicationId,
            @RequestParam(defaultValue = "fr") String lang) {
        byte[] pdfBytes = buildingPermitService.getPermitPdf(applicationId, lang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("permis-" + applicationId + "-" + lang + ".pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
