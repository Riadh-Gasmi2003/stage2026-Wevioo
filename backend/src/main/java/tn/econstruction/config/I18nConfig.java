package tn.econstruction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Détermine la langue de la requête à partir de l'en-tête HTTP
 * "Accept-Language" envoyé par le frontend à chaque appel (voir
 * frontend/src/api.js). Spring positionne alors {@code LocaleContextHolder}
 * pour toute la durée de la requête : c'est ce qui permet à Bean Validation
 * (messages de {@code @NotBlank}, etc.) et à {@link tn.econstruction.exception.BusinessException}
 * de renvoyer un message dans la bonne langue, sans code spécifique dans
 * chaque contrôleur.
 * <p>
 * Seuls le français et l'arabe sont supportés ; toute autre langue retombe
 * sur le français (langue par défaut de l'application).
 */
@Configuration
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.FRENCH);
        resolver.setSupportedLocales(List.of(Locale.FRENCH, new Locale("ar")));
        return resolver;
    }
}
