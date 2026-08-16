package tn.econstruction.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Charge les modèles d'email depuis des fichiers de traduction JSON
 * (src/main/resources/i18n/email_{lang}.json), sur le même principe que les
 * fichiers de traduction du frontend (frontend/src/i18n/locales/{lang}.json).
 * <p>
 * Chaque fichier expose des mots-clés : "greeting", "signature", et un objet
 * "types" indexé par {@link EmailType} (sujet + contenu). Le contenu des
 * traductions elles-mêmes (français, arabe...) n'est pas géré ici : ce fichier
 * ne fait que lire les mots-clés et les remplacer par les valeurs du dossier,
 * quelle que soit la langue.
 */
@Slf4j
final class EmailTemplates {

    private static final String DEFAULT_LANGUAGE = "fr";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Cache en mémoire : une langue n'est lue qu'une seule fois sur le classpath. */
    private static final Map<String, EmailMessages> CACHE = new ConcurrentHashMap<>();

    private EmailTemplates() {
    }

    /** Sujet + contenu (non substitués) pour un type d'email dans une langue donnée. */
    record Template(String subject, String body) {
    }

    /** Contenu brut d'un fichier email_{lang}.json. */
    private record EmailMessages(String greeting, String signature, Map<EmailType, TypeMessages> types) {
    }

    /** Sujet + contenu propres à un {@link EmailType}, avant assemblage avec greeting/signature. */
    private record TypeMessages(String subject, String content) {
    }

    static Template get(String language, EmailType type) {
        EmailMessages messages = load(language);
        TypeMessages typeMessages = messages.types().get(type);
        if (typeMessages == null) {
            throw new IllegalStateException(
                    "Aucun mot-clé défini pour le type " + type + " dans email_" + language + ".json");
        }
        String body = messages.greeting() + "\n\n" + typeMessages.content() + "\n\n" + messages.signature();
        return new Template(typeMessages.subject(), body);
    }

    private static EmailMessages load(String language) {
        return CACHE.computeIfAbsent(language, EmailTemplates::readFile);
    }

    private static EmailMessages readFile(String language) {
        String path = "/i18n/email_" + language + ".json";
        try (InputStream in = EmailTemplates.class.getResourceAsStream(path)) {
            if (in == null) {
                if (language.equals(DEFAULT_LANGUAGE)) {
                    throw new IllegalStateException("Fichier de traduction introuvable : " + path);
                }
                log.warn("Fichier de traduction introuvable pour la langue '{}' ({}), repli sur '{}'",
                        language, path, DEFAULT_LANGUAGE);
                return load(DEFAULT_LANGUAGE);
            }
            return MAPPER.readValue(in, EmailMessages.class);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier de traduction " + path, e);
        }
    }
}
