# TP1 — Java RMI : cabinet vétérinaire

HAI704I — Architectures logicielles distribuées · Master 1 Informatique (ICO)

## Prérequis et lancement rapide

**Environnement de développement.** macOS, VS Code (éditeur) et compilation en ligne de commande
avec `javac` / `java`, sans outil de build.

**Version de Java.** OpenJDK 21.0.12 (Homebrew). Vérification : `java -version`.

**Organisation.** Trois projets indépendants, sans `module-info.java` :
- `common` : contrat distant et types partagés (interfaces `Remote`, classes `Serializable`) ;
- `server` : servants et lanceur du serveur ;
- `client` : logique, interface en ligne de commande et lanceur ; le client dépend de
  `common` uniquement, jamais de `server`.

**Réseau.** Le registre RMI est créé dans la JVM du serveur (`LocateRegistry.createRegistry(1099)`),
sur le port **1099**. L'objet publié s'appelle `cabinet`. Par défaut le client se connecte à
`localhost:1099`.

**Compilation** (depuis la racine du dépôt) :

    javac -d common/out $(find common/src -name "*.java")
    javac -cp common/out -d server/out $(find server/src -name "*.java")
    javac -cp common/out -d client/out $(find client/src -name "*.java")

**Lancement** (un terminal par processus, dans cet ordre ; séparateur `:` sous macOS/Linux,
`;` sous Windows). Le serveur doit être redémarré après toute modification du code.

    1. java -cp common/out:server/out vete.server.Server
    2. java -cp common/out:client/out vete.client.Main            (premier client)
    3. java -cp common/out:client/out vete.client.Main            (second client, autre terminal)

Pour arrêter le serveur : `Ctrl+C`.

Les sections suivantes décrivent chaque point de contrôle (A à H).

## Point de contrôle A (étape A1)

**Objectif.** Un animal est un objet distant : il reste dans la JVM du serveur
et le client le manipule à travers un stub, obtenu depuis un registre RMI.

**Organisation.** Trois projets non modulaires :
- `common` : l'interface distante `IAnimal` (contrat partagé) ;
- `server` : le servant `AnimalImpl` et le lanceur `Server` ;
- `client` : le programme `Client`, qui ne dépend que de `common`.

**Compilation** (depuis la racine du dépôt) :

    javac -d common/out $(find common/src -name "*.java")
    javac -cp common/out -d server/out $(find server/src -name "*.java")
    javac -cp common/out -d client/out $(find client/src -name "*.java")

**Exécution** (un terminal par processus, dans cet ordre) :

    java -cp common/out:server/out vete.server.Server
    java -cp common/out:client/out vete.client.Client

Le serveur crée le registre dans sa propre JVM (port 1099) : il y a donc
deux processus, serveur et client.

**Résultat observé côté client :**

    Nom : Moon
    Proprietaire : Alex
    Race : samoyed
    Espece : chien
    Classe du stub : jdk.proxy1.$Proxy0
    Proxy dynamique ? true

**Interprétation.** Le client ne possède pas l'animal : il détient un stub, qui est
un proxy dynamique (`java.lang.reflect.Proxy`) engendré à l'exportation. Chaque
appel de méthode est transmis au servant dans la JVM du serveur.


## Point de contrôle B (étape A2)

**Objectif.** L'espèce devient une classe à part entière, transmise **par valeur** :
le client reçoit une copie, pas un moyen d'atteindre l'objet du serveur.

**Conception.**
- `Espece` (nom, espérance de vie) est dans `common` et implémente `Serializable` :
  le client et le serveur doivent tous deux connaître la classe.
- `IAnimal.getEspece()` retourne un `Espece` ; il n'existe aucune opération
  `setEspece` dans le contrat distant, donc l'espèce ne peut pas être modifiée à distance.

**Expérience (côté client).**
1. récupérer l'espèce d'un animal ;
2. la modifier localement (`setEsperanceVie(99)`) ;
3. la redemander au serveur.

**Résultat observé.**

    [client]  identityHashCode = 1170794006
    [client]  avant mutation : Espece(chien, 13 ans)
    [client]  apres mutation locale : Espece(chien, 99 ans)
    [client]  redemande au serveur : Espece(chien, 13 ans)
    [serveur] identityHashCode =  1558492786


**Interprétation.** Les deux `identityHashCode` diffèrent : il existe deux objets
Java distincts, le client manipule une copie. Après la mutation locale, le serveur
renvoie toujours la valeur d'origine (13 ans). Le serveur affiche le même
`identityHashCode` à chaque appel : il n'y a qu'un seul objet côté serveur, et
chaque appel en produit une nouvelle copie côté client.


## Point de contrôle C (étape A3)

**Décision de conception.** Le dossier de suivi est un **objet distant** : il reste
dans la JVM du serveur et le client n'en détient qu'un stub.

**Justification.** Plusieurs vétérinaires (donc plusieurs clients) doivent consulter
et modifier le même dossier. Avec un objet distant, il n'existe qu'un seul dossier,
côté serveur : toute modification est visible par tous les autres clients. Avec un
objet sérialisable, chaque client travaillerait sur sa propre copie et les
modifications ne seraient jamais partagées.

**Conception.**
- `IDossierSuivi` (dans `common`) étend `Remote` ;
- `DossierSuiviImpl` (dans `server`) est le servant, exporté ;
- `IAnimal.getDossier()` retourne un `IDossierSuivi` : c'est donc un stub qui est transmis ;
- les méthodes du servant sont `synchronized`, car plusieurs clients peuvent
  appeler en même temps ;
- `getObservations()` retourne une copie de la liste (`ArrayList`, sérialisable).

**Résultat observé (deux exécutions successives du client).**

    1re exécution :  etat avant : inconnu
                     etat apres (relu) : en observation
                     observations : [Fievre 39.5]
    2e exécution :   etat avant : en observation
                     observations : [Fievre 39.5, Fievre 39.5]

**Interprétation.** `Proxy.isProxyClass` retourne `true` pour le dossier : le client
manipule un stub. La seconde exécution du client voit la modification faite par la
première, ce qui prouve que le dossier est partagé et conservé côté serveur.

## Point de contrôle D (étape A4)

**Objectif.** Un seul objet est publié dans le registre : le cabinet vétérinaire.
Le client le récupère, puis navigue vers les animaux.

**Conception.**
- `ICabinet` (dans `common`) étend `Remote` : `getPatients()` et
  `rechercherPatient(String nom)` ;
- `CabinetImpl` (dans `server`) est le servant : il conserve la collection de
  patients ; `ajouterPatient` est une méthode locale, absente du contrat distant ;
- le serveur ne publie plus que `"cabinet"` dans le registre.

**Cohérence avec l'étape A1.** L'animal est un objet distant (exporté). Donc
`getPatients()` transmet des **stubs**, et non des copies : la liste est copiée,
mais chaque élément exporté est remplacé par son stub lors de la sérialisation.

**Recherche infructueuse.** `rechercherPatient` retourne `null` si aucun patient ne
porte ce nom ; c'est le client qui doit tester ce cas (affichage « introuvable »).

**Résultat observé.**

    Liaisons dans le registre : [cabinet]
    Nombre de patients : 2
     - Moon (chien) proxy ? true
     - Felix (chat) proxy ? true
    Recherche 'Moon' : Moon, maitre Alex
    Recherche 'Inconnu' : introuvable

**Interprétation.** Le registre ne contient qu'une seule liaison. Les patients
reçus par le client sont des proxies dynamiques : le client ne possède pas les
animaux, il les atteint à travers le cabinet.


## Point de contrôle E (étape A5)

**Objectif.** Un vétérinaire doit pouvoir enregistrer un nouveau patient depuis son poste.

**Conception.** Un `new AnimalImpl(...)` exécuté dans la JVM du client ne créerait pas
d'objet chez le serveur (et le client ne connaît d'ailleurs pas `AnimalImpl`, qui est dans
le projet `server`). Le client transmet donc seulement les **données** ; l'animal est
instancié par le serveur.

Opération ajoutée au contrat `ICabinet` :

    IAnimal enregistrerPatient(String nom, String maitre, String race, Espece espece)

| Élément | Type | Passage |
|---|---|---|
| nom, maître, race | `String` | par valeur |
| espèce | `Espece` (`Serializable`) | par valeur (copie) |
| résultat | `IAnimal` | stub (l'animal reste sur le serveur) |

**Validation.** Par interactions distantes uniquement : le nombre de patients retourné
par le serveur augmente, et `rechercherPatient("Rex")` retrouve le nouveau patient.

**Résultat observé (deux exécutions successives du client).**

    1re exécution :  Patients avant ajout : 2
                     Patient cree : Rex (proxy ? true)
                     Patients apres ajout : 3
                     Recherche 'Rex' : Rex, maitre Paul
    2e exécution :   Nombre de patients : 3   (Rex déjà présent)

**Interprétation.** L'état (le nouveau patient) est créé et conservé côté serveur : un
second lancement du client le retrouve. Le client ne détient qu'un stub.


## Point de contrôle F (étape A6)

**Objectif.** Tous les vétérinaires connectés sont prévenus automatiquement lorsque le
nombre de patients franchit les seuils 100, 500 et 1000, à la hausse comme à la baisse.

**Conception (patron Observer à travers le réseau).**
- `IObservateurCabinet` (dans `common`) étend `Remote` : opération `notifier(seuil, nombre, aLaHausse)` ;
- `ObservateurAlertes` (dans `client`) l'implémente et est **exporté** par le client :
  le client devient à son tour hébergeur d'un objet distant ;
- `ICabinet` expose `sabonner` et `seDesabonner` : le serveur reçoit le **stub** de
  l'observateur et l'ajoute à sa liste ;
- `CabinetImpl` compare le nombre de patients avant et après chaque ajout ou retrait :
  un seuil est franchi s'il se trouve entre les deux valeurs (`verifierSeuils`) ;
- une opération `retirerPatient` a été ajoutée pour pouvoir observer le franchissement à la baisse.

**Robustesse.**
- la notification de chaque observateur est protégée par son propre `try/catch` :
  une `RemoteException` (client fermé) ne bloque pas les autres, et l'observateur
  injoignable est retiré de la liste ;
- la notification travaille sur une **copie** de la liste : un abonnement pendant une
  notification ne la corrompt pas ;
- la notification est faite hors du verrou de la liste de patients.

**Test.** Deux clients en ligne de commande abonnés (option 6 du menu, deux terminaux) et `ClientRemplisseur`.

    java -cp common/out:client/out vete.client.Main                (x2, terminaux séparés)
    java -cp common/out:client/out vete.client.ClientRemplisseur

**Résultat observé.** Chaque client abonné affiche :

    [ALERTE] seuil 100 franchi a la hausse : 100 patients
    [ALERTE] seuil 100 franchi a la baisse : 99 patients

Après fermeture brutale (Ctrl+C) du premier client, le second continue de recevoir
les alertes, et le serveur affiche :

    [cabinet] observateur injoignable, retire : ConnectException

**Interprétation.** « Client » et « serveur » sont des rôles : le client héberge un objet
distant que le serveur rappelle (callback).

## Point de contrôle G (étape A7)

**Expérience.** Une sous-classe `Chien` de `Espece` est d'abord définie **uniquement dans le
projet `client`**, puis une instance est envoyée au serveur via `enregistrerPatient`.

**Échec observé (avant correction).**

    ServerException
      -> UnmarshalException : error unmarshalling arguments
        -> ClassNotFoundException : vete.client.Chien
           (no security manager: RMI class loader disabled)

**Explication.** La sérialisation Java transmet l'**état** de l'objet (les valeurs de ses champs) et le
**nom** de sa classe, mais pas son bytecode. Pour reconstruire l'objet, la JVM du serveur doit donc
charger elle-même la classe `vete.client.Chien` depuis son propre classpath. Or cette classe n'existe que
dans le projet `client` : le serveur ne la trouve pas et lève `ClassNotFoundException`, que RMI remonte
au client sous forme de `UnmarshalException` puis `ServerException`. Le chargement dynamique de classes
de RMI est désactivé ici (pas de `SecurityManager`, pas de codebase), donc rien ne peut compenser.

**Correction.** La classe `Chien` est déplacée dans `common`, connu des deux côtés.
Après correction, le patient est ajouté normalement (`Patient ajoute.`).


## Point de contrôle H : client en ligne de commande

Le client se lance avec :

    java -cp common/out:client/out vete.client.Main [hote] [port]

Par défaut : `localhost` et le port `1099`.

Menu : 1 Lister, 2 Rechercher, 3 Enregistrer, 4 Consulter le dossier, 5 Mettre à jour le dossier, 6 S'abonner aux alertes, 7 Se désabonner, 0 Quitter.

Vérifications effectuées avec deux clients simultanés :
- un patient enregistré par un client est visible par l'autre ;
- une modification du dossier de suivi est visible par l'autre client ;
- les deux clients abonnés reçoivent l'alerte `[ALERTE]` au franchissement d'un seuil ;
- les saisies invalides sont refusées sans arrêter le programme ;
- si le serveur est arrêté, le client affiche « Serveur injoignable » et le menu reste utilisable ;
- à la sortie (option 0), le client se termine proprement.
