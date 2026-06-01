package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'on cherche une tâche dont l'identifiant n'existe pas dans le système.
 */
public class TaskNotFoundException extends Exception {

    public TaskNotFoundException(String message) {
        super(message);
    }
}
