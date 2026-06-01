package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'on tente d'ajouter une tâche dont l'identifiant existe déjà.
 * Garantit l'unicité des identifiants de tâches dans le système.
 */
public class DuplicateTaskException extends Exception {

    public DuplicateTaskException(String message) {
        super(message);
    }
}
