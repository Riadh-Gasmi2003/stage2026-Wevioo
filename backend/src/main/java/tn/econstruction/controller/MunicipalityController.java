package tn.econstruction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.entity.Municipality;
import tn.econstruction.repository.MunicipalityRepository;

import java.util.List;

/**
 * Expose la liste des communes pour que le citoyen puisse en choisir une
 * au moment de la création de sa demande de permis de bâtir.
 */
@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
public class MunicipalityController {

    private final MunicipalityRepository municipalityRepository;

    @GetMapping
    public ResponseEntity<List<Municipality>> getAll() {
        return ResponseEntity.ok(municipalityRepository.findAll());
    }

    @GetMapping("/governorate/{governorate}")
    public ResponseEntity<List<Municipality>> getByGovernorate(@PathVariable String governorate) {
        return ResponseEntity.ok(municipalityRepository.findByGovernorate(governorate));
    }
}
