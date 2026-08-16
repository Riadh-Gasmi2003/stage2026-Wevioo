package tn.econstruction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.dto.UpdateLanguageDTO;
import tn.econstruction.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Mémorise la langue choisie par l'utilisateur sur son compte, afin qu'elle
     * s'applique automatiquement à la prochaine connexion (frontend, e-mails, PDF).
     */
    @PatchMapping("/{id}/language")
    public ResponseEntity<Map<String, String>> updateLanguage(
            @PathVariable Long id, @Valid @RequestBody UpdateLanguageDTO dto) {
        var user = userService.updatePreferredLanguage(id, dto.getLanguage());
        return ResponseEntity.ok(Map.of("preferredLanguage", user.getPreferredLanguage()));
    }
}
