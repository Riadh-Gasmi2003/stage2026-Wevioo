package tn.econstruction.constant;

/**
 * Constantes globales de l'application.
 * Centralise les valeurs auparavant dupliquées ou codées en dur
 * dans les services (délais légaux CATU, chemins de fichiers, formats...).
 */
public final class AppConstants {

    private AppConstants() {
    }

    // ---- Délais légaux d'instruction (loi n°94-122, art. 74 CATU) ----
    public static final int DELAI_INDIVIDUEL_JOURS = 30;
    public static final int DELAI_COLLECTIF_JOURS = 60;
    public static final int DELAI_PROLONGATION_COMPLEMENT_JOURS = 15;
    public static final int NOMBRE_MAX_RELANCES = 2;
    public static final int VALIDITE_PERMIS_ANNEES = 3;

    // ---- Stockage des fichiers ----
    public static final String UPLOAD_DIR = "uploads/";
    public static final String UPLOAD_PERMIS_DIR = "uploads/permis/";

    // ---- Formats ----
    public static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";
    public static final String NUMERO_DOSSIER_FORMAT = "DOS-%s-%06d";
    public static final String NUMERO_PERMIS_FORMAT = "PERMIS-%s-%s-%06d";
    public static final String PREFIXE_CERTIFICAT_NON_OPPOSITION = "CNO-";

    // ---- Règles métier (référencées dans le cahier des charges) ----
    public static final String RM_004_PERMIS_ACTIF_EXISTANT =
            "RM-004 : Un permis actif existe déjà pour cette référence cadastrale.";
    public static final String RM_007_MAX_COMPLEMENTS_ATTEINT =
            "RM-007 : Max 2 compléments atteints. ";
    public static final String CONDITIONS_ACCORD_TACITE =
            "Certificat de non-opposition délivré conformément à l'article 74 du CATU " +
                    "(loi n°94-122). Le délai légal d'instruction est expiré sans décision administrative.";
}
