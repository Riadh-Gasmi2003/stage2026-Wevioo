package tn.econstruction.notification;

import org.springframework.stereotype.Component;
import tn.econstruction.entity.Citizen;
import tn.econstruction.entity.PermitApplication;

/**
 * Construit le sujet/corps d'un email en assemblant les mots-clés lus dans
 * {@link EmailTemplates} (greeting / contenu du type / signature), dans la
 * langue préférée du citoyen ({@link Citizen#getPreferredLanguage()}), puis
 * remplace les variables ({{numero}}, {{prenom}}, {{nom}}, {{motif}}), avec
 * la même syntaxe d'interpolation que le frontend (i18next).
 */
@Component
public class PermitApplicationEmailContentFactory {

    public EmailContent build(EmailType type, PermitApplication application) {
        Citizen citizen = application.getCitizen();
        String language = citizen.getPreferredLanguage();

        EmailTemplates.Template template = EmailTemplates.get(language, type);
        String subject = replace(template.subject(), type, application);
        String body = replace(template.body(), type, application);
        String attachmentPath = resolveAttachment(type, application, language);
        return new EmailContent(subject, body, attachmentPath);
    }

    /**
     * Chemin du PDF à joindre selon le type d'email, ou null si aucun. Les deux
     * versions (fr/ar) existent toujours (voir JasperReportService) ; on joint
     * celle qui correspond à la langue du destinataire.
     */
    private String resolveAttachment(EmailType type, PermitApplication application, String language) {
        boolean arabic = "ar".equalsIgnoreCase(language);
        return switch (type) {
            case APPLICATION_REJECTED -> arabic && application.getRejectionNoticePdfPathAr() != null
                    ? application.getRejectionNoticePdfPathAr() : application.getRejectionNoticePdfPathFr();
            case PERMIT_ISSUED, NON_OPPOSITION_CERTIFICATE -> {
                if (application.getBuildingPermit() == null) {
                    yield null;
                }
                yield arabic && application.getBuildingPermit().getPdfPathAr() != null
                        ? application.getBuildingPermit().getPdfPathAr() : application.getBuildingPermit().getPdfPathFr();
            }
            case ADDITIONAL_DOCUMENTS_REQUIRED, APPLICATION_COMPLIANT -> null;
        };
    }

    private String replace(String text, EmailType type, PermitApplication application) {
        Citizen citizen = application.getCitizen();
        String reason = switch (type) {
            case ADDITIONAL_DOCUMENTS_REQUIRED -> valueOrEmpty(application.getAgentComment());
            case APPLICATION_REJECTED -> valueOrEmpty(application.getRejectionReason());
            case APPLICATION_COMPLIANT, PERMIT_ISSUED, NON_OPPOSITION_CERTIFICATE -> "";
        };
        return text
                .replace("{{numero}}", application.getApplicationNumber())
                .replace("{{prenom}}", citizen.getFirstName())
                .replace("{{nom}}", citizen.getLastName())
                .replace("{{motif}}", reason);
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
