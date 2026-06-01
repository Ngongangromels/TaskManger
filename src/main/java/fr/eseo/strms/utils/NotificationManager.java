package fr.eseo.strms.utils;

import fr.eseo.strms.enums.NotificationType;
import fr.eseo.strms.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestionnaire de notifications.
 *
 * Dans le cadre pédagogique, EMAIL et SMS sont simulés (la sortie va sur la console
 * avec un préfixe distinct). Seul CONSOLE produit une vraie sortie standard.
 */
public class NotificationManager {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Envoie une notification à un utilisateur via le canal spécifié.
     */
    public void notify(User user, String message, NotificationType type) {
        if (user == null || message == null || type == null) {
            return;
        }
        String time = LocalDateTime.now().format(FORMATTER);
        String prefix = switch (type) {
            case EMAIL   -> "[EMAIL  -> " + user.getEmail() + "]";
            case SMS     -> "[SMS    -> " + user.getName() + "]";
            case CONSOLE -> "[CONSOLE-> " + user.getName() + "]";
        };
        System.out.println(time + " " + prefix + " " + message);
    }
}
