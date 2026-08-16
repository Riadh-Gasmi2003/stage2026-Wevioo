package tn.econstruction.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String sender;

    @Override
    public void send(String recipient, EmailContent content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart=true dès qu'une pièce jointe doit être ajoutée ; encodage UTF-8
            // explicite pour que les accents (é, à, ç...) s'affichent correctement.
            boolean hasAttachment = content.attachmentPath() != null;
            MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachment, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(recipient);
            helper.setSubject(content.subject());
            helper.setText(content.body(), false);

            if (hasAttachment) {
                File file = new File(content.attachmentPath());
                if (file.exists()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                } else {
                    log.warn("Pièce jointe introuvable, email envoyé sans PJ : {}", file.getPath());
                }
            }

            mailSender.send(message);
            log.info("Email envoyé à {} — sujet : {}{}", recipient, content.subject(),
                    hasAttachment ? " (avec pièce jointe)" : "");

        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi de l'email : " + e.getMessage(), e);
        }
    }
}
