/**
 * Définition du module STRMS.
 *
 * Le projet est rendu MODULAIRE (Java Platform Module System, depuis Java 9)
 * pour deux raisons principales :
 *
 *  1. {@code jlink} : nous permet de produire une image runtime auto-suffisante
 *     (un mini-JRE contenant uniquement les modules dont l'app a besoin).
 *
 *  2. {@code jpackage} : nous permet de produire un installeur Windows (.exe)
 *     embarquant cette image runtime. L'utilisateur final n'a alors aucun
 *     prérequis Java à installer.
 *
 * Les directives :
 *  - {@code requires javafx.controls} : on dépend des contrôles JavaFX
 *    (qui requiert transitivement {@code javafx.graphics} et {@code javafx.base}).
 *  - {@code exports fr.eseo.strms.ui} : nécessaire pour que JavaFX puisse
 *    instancier la classe {@link fr.eseo.strms.ui.MainApp} (Application).
 *  - {@code opens ... to javafx.base} : requis par {@code PropertyValueFactory}
 *    de {@code TableView} qui utilise la réflexion pour lire les getters.
 */
module fr.eseo.strms {
    // Dépendance JavaFX (controls inclut transitivement graphics et base)
    requires javafx.controls;

    // Pour que JavaFX puisse charger la classe Application
    exports fr.eseo.strms.ui;

    // PropertyValueFactory de TableView lit les getters via réflexion
    opens fr.eseo.strms.ui.views to javafx.base;
}
