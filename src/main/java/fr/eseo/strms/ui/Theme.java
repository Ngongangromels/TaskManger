package fr.eseo.strms.ui;

/**
 * Constantes de couleurs et de styles pour l'application STRMS.
 * Charte graphique : bleu / blanc, inspirée de Jira.
 */
public final class Theme {

    private Theme() { /* classe utilitaire non instanciable */ }

    // Couleurs principales (charte bleu/blanc)
    public static final String PRIMARY_BLUE     = "#0052CC";
    public static final String PRIMARY_DARK     = "#172B4D";
    public static final String PRIMARY_LIGHT    = "#DEEBFF";
    public static final String ACCENT_BLUE      = "#0747A6";
    public static final String BG_LIGHT         = "#F4F5F7";
    public static final String BG_WHITE         = "#FFFFFF";
    public static final String TEXT_DARK        = "#172B4D";
    public static final String TEXT_MUTED       = "#5E6C84";
    public static final String BORDER_LIGHT     = "#DFE1E6";

    // Couleurs sémantiques pour les statuts
    public static final String STATUS_TODO      = "#42526E";
    public static final String STATUS_BLOCKED   = "#DE350B";
    public static final String STATUS_PROGRESS  = "#0052CC";
    public static final String STATUS_DONE      = "#36B37E";

    // Couleurs sémantiques pour les priorités
    public static final String PRIORITY_LOW       = "#36B37E";
    public static final String PRIORITY_MEDIUM    = "#FFAB00";
    public static final String PRIORITY_HIGH      = "#FF8B00";
    public static final String PRIORITY_CRITICAL  = "#DE350B";
}
