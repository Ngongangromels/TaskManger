package fr.eseo.strms.enums;

/**
 * Énumération représentant la catégorie (nature) d'une tâche.
 * Permet de classer, filtrer et faire des rapports par type.
 */
public enum TaskCategory {
    BUGFIX("Correction de bug"),
    FEATURE("Nouvelle fonctionnalité"),
    DOCUMENTATION("Documentation"),
    RESEARCH("Recherche");

    private final String label;

    TaskCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
