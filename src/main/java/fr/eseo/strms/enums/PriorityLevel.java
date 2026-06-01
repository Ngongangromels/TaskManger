package fr.eseo.strms.enums;

/**
 * Énumération représentant le niveau de priorité d'une tâche.
 *
 * L'ordre est important : les tâches CRITIQUES sont traitées en premier.
 * Chaque niveau possède un poids numérique pour le tri en PriorityQueue.
 */
public enum PriorityLevel {
    LOW(1, "Faible"),
    MEDIUM(2, "Moyenne"),
    HIGH(3, "Haute"),
    CRITICAL(4, "Critique");

    private final int weight;
    private final String label;

    PriorityLevel(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }

    /**
     * Retourne le poids numérique. Plus la valeur est grande, plus la priorité est haute.
     */
    public int getWeight() {
        return weight;
    }

    public String getLabel() {
        return label;
    }
}
