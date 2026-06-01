package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'une transition d'état invalide est tentée.
 *
 * Exemple : passer de DONE à IN_PROGRESS (DONE est un état terminal).
 */
public class InvalidTaskStateException extends Exception {

    public InvalidTaskStateException(String message) {
        super(message);
    }
}
