package fr.eseo.strms.enums;

/**
 * Énumération représentant le canal de notification utilisé pour avertir un utilisateur.
 *
 * Dans le projet pédagogique, seul CONSOLE est réellement implémenté ;
 * EMAIL et SMS sont simulés (impression console formatée).
 */
public enum NotificationType {
    EMAIL("Email"),
    SMS("SMS"),
    CONSOLE("Console");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
