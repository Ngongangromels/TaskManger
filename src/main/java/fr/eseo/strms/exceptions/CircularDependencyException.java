package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'une nouvelle dépendance créerait un cycle dans le graphe des tâches.
 *
 * Exemple : si A dépend de B et B dépend de C, ajouter "C dépend de A" produirait un cycle.
 * Le système doit alors rejeter l'opération sans modifier l'état du graphe.
 */
public class CircularDependencyException extends Exception {

    public CircularDependencyException(String message) {
        super(message);
    }

    public CircularDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
