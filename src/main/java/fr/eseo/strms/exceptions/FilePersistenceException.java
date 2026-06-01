package fr.eseo.strms.exceptions;

/**
 * Exception levée lorsqu'une opération de lecture/écriture sur un fichier échoue.
 * Sert d'enveloppe (wrapper) autour des IOException pour le métier.
 */
public class FilePersistenceException extends Exception {

    public FilePersistenceException(String message) {
        super(message);
    }

    public FilePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
