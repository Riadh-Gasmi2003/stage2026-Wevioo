package tn.econstruction.notification;

/**
 * @param attachmentPath chemin du PDF à joindre (permis, avis de refus...),
 *                       ou null si l'email n'a pas de pièce jointe.
 */
public record EmailContent(String subject, String body, String attachmentPath) {

    public EmailContent(String subject, String body) {
        this(subject, body, null);
    }
}
