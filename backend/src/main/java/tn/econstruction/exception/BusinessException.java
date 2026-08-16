package tn.econstruction.exception;

/**
 * Exception métier (dossier introuvable, action non autorisée, règle métier
 * non respectée, etc.). Contrairement à un {@code RuntimeException} classique,
 * elle ne transporte pas un texte figé mais une clé de message
 * (src/main/resources/messages*.properties) et ses éventuels arguments :
 * c'est {@link tn.econstruction.config.GlobalExceptionHandler} qui la résout
 * dans la langue de la requête ({@code Accept-Language}, voir {@link tn.econstruction.config.I18nConfig}).
 */
public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
