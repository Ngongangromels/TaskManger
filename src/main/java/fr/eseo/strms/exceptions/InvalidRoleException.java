package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'un utilisateur tente une action interdite par son rôle.
 *
 * Exemple : un Engineer qui tente d'assigner une tâche à un autre utilisateur.
 */
public class InvalidRoleException extends Exception {

    public InvalidRoleException(String message) {
        super(message);
    }
}
