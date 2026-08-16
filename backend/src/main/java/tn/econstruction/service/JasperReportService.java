package tn.econstruction.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Service;
import tn.econstruction.constant.AppConstants;
import tn.econstruction.entity.PermitApplication;
import tn.econstruction.entity.BuildingPermit;
import tn.econstruction.entity.GeneralSecretary;
import tn.econstruction.exception.BusinessException;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Génère les documents officiels (permis, certificat de non-opposition, avis de
 * refus) à partir de templates JRXML externalisés dans src/main/resources/reports/.
 * Les gabarits peuvent être modifiés dans Jaspersoft Studio sans toucher au code Java.
 * <p>
 * Chaque document est désormais généré en français <b>et</b> en arabe systématiquement,
 * quelle que soit la langue de préférence du citoyen : celui-ci doit pouvoir retélécharger
 * l'une ou l'autre version depuis l'historique de ses demandes (voir
 * BuildingPermitService / PermitApplicationService, qui stockent les deux chemins).
 * Les libellés fixes des templates sont résolus via
 * {@code reports/reports_messages_{fr,ar}.properties} ({@link JRParameter#REPORT_LOCALE}).
 * <p>
 * Le rendu de l'arabe utilise la police FreeSerif, embarquée dans le PDF (voir
 * fonts.xml / jasperreports_extension.properties, couverture du bloc Unicode arabe).
 * Dans permis_batir.jrxml, chaque ligne libellé/valeur est dupliquée en miroir
 * (libellé à droite, valeur à gauche, alignement à droite, sens de lecture RTL) et
 * affichée uniquement quand REPORT_LOCALE = ar, via printWhenExpression ; la version
 * française reste positionnée comme avant et n'est affichée que pour REPORT_LOCALE = fr.
 */
@Service
@Slf4j
public class JasperReportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_PATTERN);

    /** Chemins du PDF généré, un par langue. */
    public record GeneratedDocument(String pathFr, String pathAr) {
    }

    public GeneratedDocument generatePermitPdf(PermitApplication application, BuildingPermit permit, GeneralSecretary secretary) {
        Map<String, Object> params = new HashMap<>();
        params.put("NUMERO_PERMIS", permit.getPermitNumber());
        params.put("NOM_PETITIONNAIRE", application.getCitizen().getLastName() + " " + application.getCitizen().getFirstName());
        params.put("CIN_PETITIONNAIRE", application.getCitizen().getCin());
        params.put("REFERENCE_CADASTRALE", application.getCadastralReference());
        params.put("DESCRIPTION_TRAVAUX", application.getWorkDescription());
        params.put("SURFACE_AUTORISEE_VALUE", permit.getAuthorizedArea());
        params.put("DATE_DELIVRANCE", permit.getIssueDate().format(FMT));
        params.put("DATE_EXPIRATION", permit.getExpirationDate().format(FMT));
        params.put("COMMUNE", application.getMunicipality().getName());
        params.put("GOUVERNORAT", application.getMunicipality().getGovernorate());
        params.put("SECRETAIRE_NOM", secretary.getLastName() + " " + secretary.getFirstName());
        params.put("NUMERO_ARRETE", secretary.getDecreeNumber());
        params.put("CONDITIONS_VALUE", permit.getSpecialConditions());

        return generateBothLanguages("permis_batir", params, permit.getPermitNumber());
    }

    public GeneratedDocument generateNonOppositionCertificatePdf(PermitApplication application, BuildingPermit permit) {
        Map<String, Object> params = new HashMap<>();
        params.put("NUMERO_PERMIS", permit.getPermitNumber());
        params.put("NOM_PETITIONNAIRE", application.getCitizen().getLastName() + " " + application.getCitizen().getFirstName());
        params.put("DATE_DELIVRANCE", permit.getIssueDate().format(FMT));
        params.put("DATE_ECHEANCE_LEGALE", application.getLegalDeadline().format(FMT));
        params.put("NUMERO_DOSSIER", application.getApplicationNumber());
        params.put("COMMUNE", application.getMunicipality().getName());

        return generateBothLanguages("certificat_non_opposition", params, permit.getPermitNumber());
    }

    public GeneratedDocument generateRejectionNoticePdf(PermitApplication application) {
        Map<String, Object> params = new HashMap<>();
        params.put("NUMERO_DOSSIER", application.getApplicationNumber());
        params.put("NOM_PETITIONNAIRE", application.getCitizen().getLastName() + " " + application.getCitizen().getFirstName());
        params.put("REFERENCE_CADASTRALE", application.getCadastralReference());
        params.put("COMMUNE", application.getMunicipality().getName());
        params.put("MOTIF_REFUS", application.getRejectionReason());
        params.put("DATE_DECISION", LocalDate.now().format(FMT));

        return generateBothLanguages("avis_refus", params, "REFUS-" + application.getApplicationNumber());
    }

    private GeneratedDocument generateBothLanguages(String templateName, Map<String, Object> params, String documentNumber) {
        String pathFr = generatePdf(templateName, new HashMap<>(params), documentNumber, "fr");
        String pathAr = generatePdf(templateName, new HashMap<>(params), documentNumber, "ar");
        return new GeneratedDocument(pathFr, pathAr);
    }

    /** "fr" / "ar" -> Locale ; toute valeur inconnue ou absente retombe sur le français. */
    private Locale resolveLocale(String language) {
        if ("ar".equalsIgnoreCase(language)) {
            return new Locale("ar");
        }
        return Locale.FRENCH;
    }

    private String generatePdf(String templateName, Map<String, Object> params, String documentNumber, String language) {
        String fileName = documentNumber.replace("/", "-") + "_" + language + ".pdf";
        String outputPath = AppConstants.UPLOAD_PERMIS_DIR + fileName;

        // REPORT_LOCALE pilote la résolution des libellés $R{...} dans le gabarit
        // (reports/reports_messages_fr.properties / reports_messages_ar.properties)
        // ET le choix de la mise en page (miroir à droite) dans permis_batir.jrxml
        // via les printWhenExpression conditionnées sur cette même locale.
        params.put(JRParameter.REPORT_LOCALE, resolveLocale(language));

        try {
            Files.createDirectories(Paths.get(AppConstants.UPLOAD_PERMIS_DIR));

            try (InputStream jrxmlStream = getClass().getResourceAsStream("/reports/" + templateName + ".jrxml")) {
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
            }

            log.info("PDF généré ({}, langue={}) : {}", templateName, language, outputPath);
            return outputPath;

        } catch (Exception e) {
            log.error("Erreur génération PDF ({}, langue={}) : {}", templateName, language, e.getMessage());
            throw new BusinessException("error.report.generationFailed", e.getMessage());
        }
    }
}
