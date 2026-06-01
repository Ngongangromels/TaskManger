# 📦 Guide d'installation et de lancement — STRMS

> **Public visé** : tous les membres de l'équipe (dev), peu importe l'OS (Windows, macOS, Linux).
> Cette méthode est **universelle** et requiert un seul prérequis : un JDK 17 ou supérieur.

---

## Sommaire

- [1. Pourquoi cette méthode ?](#1-pourquoi-cette-méthode)
- [2. Prérequis (à installer une seule fois)](#2-prérequis-à-installer-une-seule-fois)
- [3. Lancer le projet sous Windows](#3-lancer-le-projet-sous-windows)
- [4. Lancer le projet sous macOS](#4-lancer-le-projet-sous-macos)
- [5. Lancer le projet sous Linux](#5-lancer-le-projet-sous-linux)
- [6. Commandes utiles](#6-commandes-utiles)
- [7. Résolution de problèmes](#7-résolution-de-problèmes)

---

## 1. Pourquoi cette méthode ?

Le projet utilise le **Maven Wrapper** (`mvnw` / `mvnw.cmd`). C'est un petit script
qui télécharge automatiquement la **bonne version de Maven** pour le projet.

**Avantages** :
- ✅ Vous n'avez **pas besoin** d'installer Maven manuellement
- ✅ Tous les contributeurs utilisent **exactement la même version** de Maven
- ✅ Maven télécharge ensuite **JavaFX adapté à votre OS** automatiquement
  (les binaires natifs Windows, Mac Intel, Mac Apple Silicon ou Linux selon le cas)
- ✅ Approche **standard** dans le monde Java open-source

Vous n'avez donc **qu'une seule chose à installer** : un JDK 17 (ou plus récent).

---

## 2. Prérequis (à installer une seule fois)

### 2.1 Installer un JDK 17 ou supérieur

Choisissez **l'une** de ces deux distributions gratuites (peu importe laquelle) :

#### Option recommandée — Eclipse Adoptium (Temurin)
- 🌐 https://adoptium.net/temurin/releases/?version=17
- Choisir : **OS** = votre système, **Architecture** = x64 (ou aarch64 pour Mac M1/M2/M3),
  **Package Type** = **JDK**, **Version** = 17 LTS
- Télécharger le fichier `.msi` (Windows), `.pkg` (macOS) ou `.tar.gz` (Linux)
- Lancer l'installeur et **cocher "Set JAVA_HOME variable"** s'il est proposé

#### Alternative — Liberica
- 🌐 https://bell-sw.com/pages/downloads/
- Idem : choisir JDK 17, votre OS, votre architecture

### 2.2 Vérifier l'installation

Ouvrez un terminal (PowerShell sur Windows, Terminal sur macOS/Linux) et tapez :

```bash
java -version
```

Vous devez voir une réponse du type :
```
openjdk version "17.0.x" 2024-xx-xx
OpenJDK Runtime Environment ...
```

Si `java -version` n'est pas reconnu, il faut **redémarrer votre terminal**
(et/ou votre PC) pour que la variable `PATH` soit prise en compte.

---

## 3. Lancer le projet sous Windows

### 3.1 Récupérer le projet

Si vous utilisez Git :
```bat
git clone <url-du-repo>
cd "projet complet java fx"
```

Sinon, dézippez l'archive du projet et ouvrez le dossier dans une fenêtre PowerShell
ou CMD (clic-droit dans le dossier → **Ouvrir dans le terminal**).

### 3.2 Lancer l'application

```bat
mvnw.cmd javafx:run
```

Ou simplement double-cliquer sur **`run.bat`** à la racine du projet.

> 🕐 **Premier lancement** : Maven télécharge ~30 Mo de dépendances
> (Maven 3.9.9 + JavaFX). Patientez 1 à 2 minutes.
> Les lancements suivants prendront ~5 secondes.

### 3.3 Lancer les tests JUnit

```bat
mvnw.cmd test
```

Ou double-cliquer sur **`run-tests.bat`**.

---

## 4. Lancer le projet sous macOS

### 4.1 Récupérer le projet

```bash
git clone <url-du-repo>
cd "projet complet java fx"
```

### 4.2 Rendre le wrapper exécutable (uniquement la première fois)

```bash
chmod +x mvnw run.sh run-tests.sh
```

### 4.3 Lancer l'application

```bash
./mvnw javafx:run
```

Ou :
```bash
./run.sh
```

### 4.4 Lancer les tests

```bash
./mvnw test
```

> 💡 **Note Apple Silicon (M1/M2/M3)** : Maven détecte automatiquement votre
> processeur et télécharge `javafx-controls-21.0.2-mac-aarch64.jar`. Aucune
> configuration manuelle n'est nécessaire.

> ⚠️ **Note Gatekeeper** : si macOS affiche un avertissement de sécurité
> au premier lancement de Java, allez dans **Réglages Système ▸ Confidentialité**
> et autorisez Java une fois.

---

## 5. Lancer le projet sous Linux

### 5.1 Récupérer le projet et le rendre exécutable

```bash
git clone <url-du-repo>
cd "projet complet java fx"
chmod +x mvnw run.sh run-tests.sh
```

### 5.2 Lancer l'application

```bash
./mvnw javafx:run
```

### 5.3 Lancer les tests

```bash
./mvnw test
```

> 💡 **Linux + Wayland** : si vous rencontrez un problème d'affichage,
> lancez avec `GDK_BACKEND=x11 ./mvnw javafx:run`.

---

## 6. Commandes utiles

| Commande                              | Description                                    |
|---------------------------------------|------------------------------------------------|
| `mvnw javafx:run` (`./mvnw javafx:run`) | Lance l'application                          |
| `mvnw test`                           | Lance les tests JUnit                          |
| `mvnw clean`                          | Supprime le dossier `target/`                  |
| `mvnw clean test`                     | Nettoie puis relance les tests                 |
| `mvnw compile`                        | Compile sans lancer                            |
| `mvnw javafx:jlink`                   | Produit une image runtime auto-suffisante      |

---

## 7. Résolution de problèmes

### "Java is not recognized" / "command not found: java"

Vous n'avez pas installé un JDK ou il n'est pas dans le `PATH`.
- Réinstallez le JDK en cochant l'option "Set JAVA_HOME"
- Redémarrez votre terminal après installation

### "BUILD FAILURE" sans détail au premier lancement

Vérifiez votre **connexion Internet** : le wrapper doit télécharger Maven et JavaFX.

### Erreur "Cannot create resource output directory: ...\target\classes" sous Windows

C'est **Windows Defender — "Accès contrôlé aux dossiers"** qui bloque
la création de dossiers dans `Documents`. Trois solutions :

1. **Le plus simple** : déplacer le projet hors de `Documents`,
   par exemple `C:\Dev\strms`
2. Aller dans **Sécurité Windows ▸ Protection contre les virus et menaces ▸
   Gérer la protection contre les ransomwares ▸ Accès contrôlé aux dossiers**
   et désactiver temporairement
3. Autoriser explicitement `mvnw.cmd` et `java.exe` via **"Autoriser une appli
   via l'accès contrôlé aux dossiers"**

### Erreur "Une stratégie de contrôle d'application a bloqué ce fichier"

C'est **Smart App Control** sur Windows 11 qui bloque un binaire non signé.
Si vous avez ce problème en lançant `java.exe` :
- Utiliser un JDK signé par l'éditeur (Eclipse Adoptium est signé Microsoft)
- Ou désactiver Smart App Control (paramètres ▸ Sécurité Windows ▸
  Contrôle des applis et du navigateur)

### Tests qui échouent uniquement sur ma machine

Vérifiez votre version de Java :
```bash
java -version
```
Le projet exige **Java 17 minimum**. Si vous avez Java 8 ou Java 11,
mettez à jour.

### "Module not found: javafx.controls"

JavaFX n'est pas téléchargé. Lancez :
```bash
mvnw clean compile
```
Maven retéléchargera tout.

---

## En une commande

Si tout est installé, voici le **résumé minimal** pour lancer le projet :

**Windows :**
```bat
mvnw.cmd javafx:run
```

**macOS / Linux :**
```bash
./mvnw javafx:run
```

C'est tout. 🚀
