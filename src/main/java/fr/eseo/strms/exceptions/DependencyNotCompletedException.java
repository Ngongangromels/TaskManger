package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'on tente de démarrer ou terminer une tâche
 * dont les prérequis (dépendances) ne sont pas encore au statut DONE.
 */
public class DependencyNotCompletedException extends Exception {

    public DependencyNotCompletedException(String message) {
        super(message);
    }
}
