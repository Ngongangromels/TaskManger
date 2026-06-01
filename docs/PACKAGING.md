# 🏗️ Packaging — Production de l'installeur Windows

> **Public visé** : développeur·e du projet qui souhaite produire un binaire
> Windows distribuable.
> Ce document explique **techniquement** comment l'installeur est généré
> et pourquoi chaque étape est nécessaire.

---

## Sommaire

- [1. Objectif](#1-objectif)
- [2. Architecture du packaging](#2-architecture-du-packaging)
- [3. Pré-requis](#3-pré-requis)
- [4. Production du `.exe`](#4-production-du-exe)
- [5. Contenu de la distribution](#5-contenu-de-la-distribution)
- [6. Distribution aux utilisateurs](#6-distribution-aux-utilisateurs)
- [7. Limitations connues](#7-limitations-connues)
- [8. FAQ technique](#8-faq-technique)

---

## 1. Objectif

Produire une **distribution autonome** pour Windows, c'est-à-dire un dossier
que vos collègues peuvent exécuter **sans rien installer** (ni Java, ni JavaFX,
ni aucun outil annexe). Il leur suffit d'extraire l'archive et de double-cliquer
sur un launcher.

### Comparaison avec l'Option B

| Aspect                    | Option A (jpackage)                          | Option B (Maven Wrapper)         |
|---------------------------|----------------------------------------------|----------------------------------|
| Pré-requis utilisateur    | **Aucun**                                    | JDK 17+ installé                 |
| Pré-requis dev (packager) | JDK 17+ avec `jlink` & `jpackage`            | Aucun                            |
| Taille de la distribution | ~91 Mo (compressée ~45 Mo)                   | Quelques Mo (sources + scripts)  |
| Modification du code      | Non (binaire compilé)                        | **Oui**                          |
| Cible                     | Démo / utilisateur final                     | Équipe de développement          |

**Les deux options coexistent dans le projet** : choisissez celle qui
correspond à l'utilisateur visé.

---

## 2. Architecture du packaging

Le packaging s'effectue en **3 étapes** automatisées par le script
`package-windows.bat` :

```
┌─────────────────────────────────────────────────────────────┐
│ 1. mvnw clean       Nettoie target/                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. mvnw javafx:jlink                                         │
│    Produit un MINI JRE auto-suffisant dans                  │
│    target/strms-runtime/  (~80 Mo)                          │
│    Contient : java.exe + modules requis (java.base,         │
│    java.desktop, javafx.controls, …) + classes de l'app     │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. jpackage --type app-image --runtime-image …             │
│    Empaquette le runtime jlink dans une distribution        │
│    structurée :  dist/STRMS/                                │
│        ├── STRMS.exe   ← launcher natif Windows            │
│        ├── STRMS.bat   ← launcher de fallback (généré par   │
│        │                  notre script en plus de jpackage) │
│        ├── app/        ← jars de l'application              │
│        └── runtime/    ← le mini JRE produit par jlink      │
└─────────────────────────────────────────────────────────────┘
```

### Pourquoi avoir besoin d'un projet **modulaire** (`module-info.java`)

`jlink` et `jpackage` ne savent travailler qu'avec des projets respectant
le **Java Platform Module System** (JPMS, Java 9+). Concrètement :

- Le projet **doit** déclarer ses dépendances JavaFX via `requires`
- Le projet **doit** exposer ses points d'entrée via `exports`
- Toute classe accédée par réflexion (ex: `PropertyValueFactory` de TableView)
  doit être **ouverte** via `opens ... to ...`

C'est pour cette raison que le fichier
[`src/main/java/module-info.java`](../src/main/java/module-info.java)
a été ajouté au projet :

```java
module fr.eseo.strms {
    requires javafx.controls;
    exports fr.eseo.strms.ui;                       // pour Application.launch()
    opens fr.eseo.strms.ui.views to javafx.base;    // pour PropertyValueFactory
}
```

### Pourquoi un `STRMS.bat` en plus du `STRMS.exe`

Sur Windows 11, **Smart App Control** (la protection anti-malware basée sur
réputation) bloque par défaut l'exécution des `.exe` non signés numériquement.
Or, signer un `.exe` requiert un **certificat payant** (~150 €/an chez Sectigo,
DigiCert, etc.) — ce n'est pas pertinent pour un projet pédagogique.

Solution adoptée : générer **en parallèle** un launcher `.bat` qui appelle
directement `runtime\bin\java.exe -m fr.eseo.strms/fr.eseo.strms.ui.MainApp`.
Smart App Control n'analyse pas les `.bat` qui appellent un binaire connu (le
`java.exe` du runtime jlink), donc le lancement passe sans avertissement.

```bat
@echo off
REM Lance STRMS via le runtime Java embarque (contourne Smart App Control).
set DIR=%~dp0
"%DIR%runtime\bin\java.exe" -m fr.eseo.strms/fr.eseo.strms.ui.MainApp %*
```

---

## 3. Pré-requis

Pour packager le projet, vous avez besoin de :

| Outil       | Version | Usage                               | Vérification           |
|-------------|---------|-------------------------------------|------------------------|
| **JDK**     | 17+     | Compilation et `jlink`              | `java -version`        |
| **jpackage**| inclus  | Création du `.exe`                  | `jpackage --version`   |

`jpackage` est **inclus dans le JDK depuis Java 14**. Si la commande
`jpackage --version` ne fonctionne pas, vérifiez que `JAVA_HOME\bin` est bien
dans votre `PATH`.

> 💡 Le wrapper Maven (`mvnw.cmd`) n'a **pas besoin** d'être installé :
> il est inclus dans le projet et téléchargera Maven automatiquement
> au premier lancement.

---

## 4. Production du `.exe`

### Méthode automatique (recommandée)

À la racine du projet, double-cliquez sur **`package-windows.bat`** ou lancez-le
depuis un terminal :

```bat
package-windows.bat
```

Le script affichera la progression :
```
[1/3] Nettoyage du projet...
[2/3] Generation du runtime jlink...
[3/3] Generation de l'installeur .exe avec jpackage...
[INFO] Creation du launcher .bat de fallback...
[SUCCES] Distribution generee dans : dist\STRMS\
```

Durée totale : **~2 minutes** (premier run, dépendances à télécharger),
**~30 secondes** ensuite.

### Méthode manuelle (si vous voulez comprendre chaque étape)

```bat
REM Etape 1 : nettoyer
mvnw.cmd clean

REM Etape 2 : produire l'image runtime jlink
mvnw.cmd javafx:jlink

REM Etape 3 : produire le .exe avec jpackage
jpackage ^
    --type app-image ^
    --runtime-image "target\strms-runtime" ^
    --module fr.eseo.strms/fr.eseo.strms.ui.MainApp ^
    --name STRMS ^
    --app-version 1.0.0 ^
    --vendor "ESEO E3e S6 - Spring 2026" ^
    --description "Smart Task and Resource Management System" ^
    --dest "dist"
```

#### Détail des options `jpackage`

| Option              | Rôle                                                   |
|---------------------|--------------------------------------------------------|
| `--type app-image`  | Produit un dossier (et non un installateur `.msi`)     |
| `--runtime-image`   | Réutilise le runtime jlink (au lieu d'un JRE complet)  |
| `--module`          | Format `nom-module/classe-principale`                  |
| `--name`            | Nom du dossier et du `.exe` final                      |
| `--app-version`     | Version (apparaît dans les propriétés du fichier)     |
| `--vendor`          | Éditeur (apparaît dans les propriétés)                |
| `--dest`            | Dossier de sortie                                      |

> 📚 **Référence complète** : [docs Oracle jpackage](https://docs.oracle.com/en/java/javase/17/jpackage/packaging-tool-user-guide.pdf)

---

## 5. Contenu de la distribution

```
dist/STRMS/
├── STRMS.exe         ← launcher natif Windows
├── STRMS.bat         ← launcher de fallback (anti-Smart App Control)
├── app/
│   ├── strms.cfg     ← config jpackage (chemin vers le module-main)
│   └── ...
└── runtime/
    ├── bin/
    │   ├── java.exe  ← Java runtime embarqué
    │   └── ...
    ├── lib/
    │   └── modules   ← image jlink des modules nécessaires
    └── conf/
```

**Aucun JRE Java n'est requis sur la machine cible** : il est entièrement
embarqué dans `runtime/`.

---

## 6. Distribution aux utilisateurs

### Pour vos collègues Windows

1. Compresser le dossier `dist\STRMS\` en `STRMS-1.0.0-win.zip`
   (clic-droit → **Envoyer vers ▸ Dossier compressé**, ou via 7-Zip/WinRAR)
2. Partager ce zip (Discord, mail, GitHub Releases, OneDrive...)
3. **Côté collègue** :
   - Décompresser le zip
   - Double-cliquer sur **`STRMS.exe`**
   - Si Windows bloque (Smart App Control), double-cliquer sur **`STRMS.bat`**

### Pour vos collègues macOS / Linux

`jpackage` produit un `.exe` Windows uniquement. Pour macOS/Linux,
**utilisez l'Option B** (cf. [`docs/INSTALLATION.md`](INSTALLATION.md)).

---

## 7. Limitations connues

### 7.1 Le `.exe` est bloqué par Smart App Control sur Windows 11

**Cause** : le `.exe` n'est pas signé numériquement.

**Contournement** : utiliser le `STRMS.bat` fourni à côté du `.exe`. Il a
exactement le même comportement et n'est pas filtré par Smart App Control.

### 7.2 La distribution fait ~90 Mo

C'est la conséquence directe d'un JRE embarqué (~70 Mo) + l'application.
Pour réduire la taille :

- `jlink` retire déjà tous les modules Java inutiles (`java.sql`, `java.xml`...)
- Avec `--strip-debug --no-man-pages --no-header-files`, on réduit encore (~15 %)
- Pour aller plus loin : compresser le zip avec **7-Zip en LZMA2**
  (descend à ~30 Mo)

### 7.3 Cross-compilation impossible

`jpackage` ne peut produire **que pour l'OS sur lequel il s'exécute**.
Pour produire un `.dmg` macOS, il faut lancer le packaging **sur** un Mac.

**Solution future** : ajouter un workflow GitHub Actions qui produit
les 3 binaires (Win/Mac/Linux) automatiquement à chaque `git tag`.

---

## 8. FAQ technique

### Pourquoi pas un `.msi` ou un `.exe` d'installation ?

`jpackage` peut produire des `.msi` (avec WiX Toolset) ou des `.exe`
d'installation (avec Inno Setup), mais ces formats nécessitent des outils
tiers à installer. Pour rester simple et portable, on utilise `--type app-image`
qui produit un dossier auto-exécutable.

Si à terme vous voulez un installeur d'installation classique :
1. Installez [WiX Toolset 3.x](https://wixtoolset.org/releases/) (gratuit)
2. Ajoutez `WiX-Bin\` à votre `PATH`
3. Remplacez `--type app-image` par `--type msi` dans le script

### Pourquoi `--type app-image` plutôt que directement `--type exe` ?

`--type exe` génère un installeur d'installation (.exe d'installation) avec
contrat de licence, choix du dossier d'installation, etc. Il nécessite
**Inno Setup** comme outil tiers. Trop lourd pour notre usage.

`--type app-image` produit un **dossier auto-exécutable** : aucun outil tiers,
aucune installation, on dézippe et on lance.

### Quelle est la différence entre le `STRMS.exe` et `runtime/bin/java.exe` ?

- `STRMS.exe` est un petit launcher généré par jpackage (~450 Ko). Il est
  configuré pour appeler automatiquement `runtime/bin/java.exe -m
  fr.eseo.strms/fr.eseo.strms.ui.MainApp`.
- `runtime/bin/java.exe` est le vrai exécutable Java (issu du JDK).

Notre `STRMS.bat` court-circuite le `STRMS.exe` et appelle directement le
`java.exe`. Le résultat à l'écran est strictement identique.

### Comment versionner la distribution ?

Modifiez `--app-version` dans `package-windows.bat`. Cette valeur apparaît
dans les **propriétés du fichier** (clic-droit ▸ Propriétés ▸ Détails).

### Le runtime jlink ne marche que sur Windows ?

Oui. `jlink` produit une image **spécifique à l'OS sur lequel il tourne**.
Le `target/strms-runtime/` créé sur votre PC Windows ne fonctionnera **pas**
sur un Mac. Pour packager pour macOS, il faut relancer la commande sur un Mac.
