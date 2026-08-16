package tn.econstruction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.notification.EmailContent;
import tn.econstruction.notification.EmailService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationTestController {

    private final EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<String> testSend(@RequestParam String recipient) {
        try {
            emailService.send(recipient, new EmailContent(
                    "Test SMTP — e-Construction Tunisie",
                    "Si vous recevez cet email, la configuration SMTP fonctionne correctement."
            ));
            return ResponseEntity.ok("Email de test envoyé à " + recipient);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email de test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec de l'envoi : " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
    }
}
