package vete.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ConsoleUI {

    // Levee quand l'entree est terminee (Ctrl+D) : traitee comme une demande de sortie.
    private static class FinDeSaisie extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final ClientCabinet logique;
    private final BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));

    public ConsoleUI(ClientCabinet logique) {
        this.logique = logique;
        // Les alertes arrivent sur un fil du runtime RMI, a tout moment : le prefixe [ALERTE] les distingue.
        this.logique.definirAfficheurAlertes(message -> System.out.println("\n" + message));
    }

    public void lancer() {
        try {
            boolean continuer = true;
            while (continuer) {
                afficherMenu();
                String choix = lireLigne("Votre choix : ").trim();
                switch (choix) {
                    case "1": actionLister(); break;
                    case "2": actionRechercher(); break;
                    case "3": actionEnregistrer(); break;
                    case "4": actionConsulterDossier(); break;
                    case "5": actionMettreAJourDossier(); break;
                    case "6": actionAbonner(); break;
                    case "7": actionDesabonner(); break;
                    case "0": continuer = false; break;
                    default:
                        System.out.println("Choix inconnu : \"" + choix + "\". Entrez un chiffre du menu.");
                }
            }
        } catch (FinDeSaisie e) {
            System.out.println("\nFin de la saisie.");
        } finally {
            logique.fermer();
            System.out.println("Au revoir.");
        }
    }

    private void afficherMenu() {
        System.out.println();
        System.out.println("=== Cabinet veterinaire ===");
        System.out.println("1. Lister les patients");
        System.out.println("2. Rechercher un patient");
        System.out.println("3. Enregistrer un patient");
        System.out.println("4. Consulter le dossier de suivi");
        System.out.println("5. Mettre a jour le dossier de suivi");
        System.out.println("6. S'abonner aux alertes");
        System.out.println("7. Se desabonner des alertes");
        System.out.println("0. Quitter");
    }

    // ---- lecture et validation des saisies ----

    private String lireLigne(String invite) {
        System.out.print(invite);
        System.out.flush();
        try {
            String ligne = entree.readLine();
            if (ligne == null) {
                throw new FinDeSaisie();
            }
            return ligne;
        } catch (IOException e) {
            throw new FinDeSaisie();
        }
    }

    private String lireNonVide(String invite) {
        while (true) {
            String saisie = lireLigne(invite).trim();
            if (!saisie.isEmpty()) {
                return saisie;
            }
            System.out.println("La saisie ne peut pas etre vide.");
        }
    }

    private int lireEntierPositif(String invite) {
        while (true) {
            String saisie = lireNonVide(invite);
            try {
                int valeur = Integer.parseInt(saisie);
                if (valeur > 0) {
                    return valeur;
                }
                System.out.println("Entrez un entier strictement positif.");
            } catch (NumberFormatException e) {
                System.out.println("Ce n'est pas un nombre entier valide : " + saisie);
            }
        }
    }

    // ---- actions du menu ----

    private void actionLister() {
        try {
            List<String> patients = logique.listerPatients();
            if (patients.isEmpty()) {
                System.out.println("Aucun patient.");
                return;
            }
            System.out.println(patients.size() + " patient(s) :");
            for (String p : patients) {
                System.out.println(" - " + p);
            }
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionRechercher() {
        String nom = lireNonVide("Nom du patient : ");
        try {
            String resultat = logique.rechercherPatient(nom);
            if (resultat == null) {
                System.out.println("Aucun patient ne porte ce nom : " + nom);
            } else {
                System.out.println(resultat);
            }
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionEnregistrer() {
        String nom = lireNonVide("Nom du patient : ");
        String maitre = lireNonVide("Nom du maitre : ");
        String race = lireNonVide("Race : ");
        String espece = lireNonVide("Espece (ex. chien, chat) : ");
        int esperance = lireEntierPositif("Esperance de vie moyenne de l'espece (ans) : ");
        try {
            logique.enregistrerPatient(nom, maitre, race, espece, esperance);
            System.out.println("Patient enregistre : " + nom);
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionConsulterDossier() {
        String nom = lireNonVide("Nom du patient : ");
        try {
            System.out.println(logique.consulterDossier(nom));
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionMettreAJourDossier() {
        String nom = lireNonVide("Nom du patient : ");
        System.out.println("1. Modifier l'etat de sante");
        System.out.println("2. Ajouter une observation");
        String choix = lireLigne("Votre choix : ").trim();
        try {
            if (choix.equals("1")) {
                String etat = lireNonVide("Nouvel etat de sante : ");
                logique.modifierEtat(nom, etat);
                System.out.println("Etat de sante mis a jour.");
            } else if (choix.equals("2")) {
                String observation = lireNonVide("Observation : ");
                logique.ajouterObservation(nom, observation);
                System.out.println("Observation ajoutee.");
            } else {
                System.out.println("Choix inconnu : \"" + choix + "\".");
            }
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionAbonner() {
        try {
            logique.sabonner();
            System.out.println("Vous etes abonne aux alertes.");
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }

    private void actionDesabonner() {
        try {
            logique.seDesabonner();
            System.out.println("Vous etes desabonne des alertes.");
        } catch (ErreurCabinet e) {
            System.out.println(e.getMessage());
        }
    }
}