package tn.econstruction.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.econstruction.entity.PermitApplication;
import tn.econstruction.repository.PermitApplicationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermitApplicationEmailListener {

    private final PermitApplicationRepository permitApplicationRepository;
    private final PermitApplicationEmailContentFactory emailContentFactory;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEmailEvent(PermitApplicationEmailEvent event) {
        try {
            PermitApplication application = permitApplicationRepository.findById(event.getApplicationId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Dossier introuvable pour l'envoi d'email : " + event.getApplicationId()));

            EmailContent content = emailContentFactory.build(event.getType(), application);
            emailService.send(application.getCitizen().getEmail(), content);

        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email ({}) pour le dossier id={}",
                    event.getType(), event.getApplicationId(), e);
        }
    }
}
