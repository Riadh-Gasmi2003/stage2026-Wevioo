package tn.econstruction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tn.econstruction.dto.*;
import tn.econstruction.enums.ApplicationStatus;
import tn.econstruction.enums.DocumentType;
import tn.econstruction.service.PermitApplicationService;
import tn.econstruction.service.BuildingPermitService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permit-applications")
@RequiredArgsConstructor
public class PermitApplicationController {

    private final PermitApplicationService permitApplicationService;
    private final BuildingPermitService buildingPermitService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PermitApplicationDTO> submit(
            @RequestPart("dossier") @Valid PermitApplicationCreateDTO dto,
            @RequestParam Long citizenId,
            MultipartHttpServletRequest request) {
        return ResponseEntity.status(201)
                .body(permitApplicationService.submitApplication(dto, citizenId, extractDocuments(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermitApplicationDTO> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(permitApplicationService.getApplicationById(id));
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<PermitApplicationDTO>> getMyApplications(@PathVariable Long citizenId) {
        return ResponseEntity.ok(permitApplicationService.getMyApplications(citizenId));
    }

    @GetMapping("/municipality/{municipalityId}")
    public ResponseEntity<List<PermitApplicationDTO>> getMunicipalityApplications(@PathVariable Long municipalityId) {
        return ResponseEntity.ok(permitApplicationService.getMunicipalityApplications(municipalityId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PermitApplicationDTO>> getByStatus(@PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(permitApplicationService.getApplicationsByStatus(status));
    }

    @PutMapping(value = "/{id}/additional-documents", consumes = "multipart/form-data")
    public ResponseEntity<PermitApplicationDTO> provideAdditionalDocuments(
            @PathVariable Long id,
            MultipartHttpServletRequest request) {
        return ResponseEntity.ok(permitApplicationService.provideAdditionalDocuments(id, extractDocuments(request)));
    }

    @PostMapping("/{id}/start-review")
    public ResponseEntity<PermitApplicationDTO> startReview(
            @PathVariable Long id, @RequestParam Long agentId) {
        return ResponseEntity.ok(permitApplicationService.startReview(id, agentId));
    }

    @PostMapping("/{id}/compliance")
    public ResponseEntity<PermitApplicationDTO> markAsCompliant(
            @PathVariable Long id, @RequestParam Long agentId) {
        return ResponseEntity.ok(permitApplicationService.markAsCompliant(id, agentId));
    }

    @PostMapping("/{id}/request-additional-documents")
    public ResponseEntity<PermitApplicationDTO> requestAdditionalDocuments(
            @PathVariable Long id, @Valid @RequestBody AdditionalDocumentsRequestDTO dto) {
        return ResponseEntity.ok(permitApplicationService.requestAdditionalDocuments(id, dto));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PermitApplicationDTO> approve(
            @PathVariable Long id, @RequestParam Long secretaryId) {
        PermitApplicationDTO application = permitApplicationService.approveApplication(id, secretaryId);
        buildingPermitService.generatePermit(id, secretaryId);
        return ResponseEntity.ok(application);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PermitApplicationDTO> reject(
            @PathVariable Long id, @Valid @RequestBody RejectionDTO dto) {
        return ResponseEntity.ok(permitApplicationService.rejectApplication(id, dto));
    }

    @GetMapping("/{id}/rejection-notice/pdf")
    public ResponseEntity<byte[]> getRejectionNoticePdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "fr") String lang) {
        byte[] pdf = permitApplicationService.getRejectionNoticePdf(id, lang);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("avis-refus-" + id + "-" + lang + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // Chaque input file du formulaire porte en nom le type de document (ex: "PLAN_SITUATION")
    private Map<DocumentType, MultipartFile> extractDocuments(MultipartHttpServletRequest request) {
        Map<DocumentType, MultipartFile> documents = new EnumMap<>(DocumentType.class);
        request.getFileMap().forEach((name, file) -> {
            try {
                documents.put(DocumentType.valueOf(name), file);
            } catch (IllegalArgumentException ignored) {
                // champ de fichier non reconnu comme type de document, ignoré
            }
        });
        return documents;
    }
}
