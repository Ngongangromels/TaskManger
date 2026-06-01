package fr.eseo.strms.utils;

/**
 * Interface fonctionnelle représentant la capacité de générer un rapport.
 *
 * Toute classe qui produit un rapport (texte, console, fichier) doit l'implémenter.
 * Permet d'illustrer le polymorphisme : on peut traiter de la même façon
 * différentes sortes de rapports (rapport tâches, rapport utilisateurs, etc.).
 */
public interface Reportable {

    /**
     * Génère le contenu du rapport sous forme de chaîne de caractères.
     * @return le rapport prêt à être affiché ou enregistré dans un fichier.
     */
    String generateReport();
}
