package vete.client;

import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

import vete.common.Espece;
import vete.common.IAnimal;
import vete.common.ICabinet;
import vete.common.IDossierSuivi;

public class ClientCabinet {

private final ICabinet cabinet;

private ObservateurAlertes observateur;
    
    private volatile Consumer<String> afficheurAlertes = message -> { };

    public ClientCabinet(String hote, int port) throws ErreurCabinet {
        try {
            Registry registry = LocateRegistry.getRegistry(hote, port);
            this.cabinet = (ICabinet) registry.lookup("cabinet");
        } catch (NotBoundException e) {
            throw new ErreurCabinet("Le cabinet n'est pas publie dans le registre.");
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public List<String> listerPatients() throws ErreurCabinet {
        try {
            List<String> lignes = new ArrayList<>();
            for (IAnimal a : cabinet.getPatients()) {
                lignes.add(decrire(a));
            }
            return lignes;
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    // Retourne null si aucun patient ne porte ce nom.
    public String rechercherPatient(String nom) throws ErreurCabinet {
        try {
            IAnimal a = cabinet.rechercherPatient(nom);
            return (a == null) ? null : decrire(a);
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public void enregistrerPatient(String nom, String maitre, String race,
            String nomEspece, int esperanceVie) throws ErreurCabinet {
        try {
            cabinet.enregistrerPatient(nom, maitre, race, new Espece(nomEspece, esperanceVie));
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public String consulterDossier(String nom) throws ErreurCabinet {
        try {
            IDossierSuivi dossier = dossierDe(nom);
            return "Etat de sante : " + dossier.getEtatSante()
                    + "\nObservations : " + dossier.getObservations();
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public void modifierEtat(String nom, String etat) throws ErreurCabinet {
        try {
            dossierDe(nom).setEtatSante(etat);
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public void ajouterObservation(String nom, String observation) throws ErreurCabinet {
        try {
            dossierDe(nom).ajouterObservation(observation);
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

        public void definirAfficheurAlertes(Consumer<String> afficheur) {
        this.afficheurAlertes = afficheur;
    }

    public synchronized boolean estAbonne() {
        return observateur != null;
    }

    public synchronized void sabonner() throws ErreurCabinet {
        if (observateur != null) {
            throw new ErreurCabinet("Vous etes deja abonne aux alertes.");
        }
        try {
            ObservateurAlertes obs = new ObservateurAlertes(m -> afficheurAlertes.accept(m));
            try {
                cabinet.sabonner(obs);
            } catch (RemoteException e) {
                desexporter(obs);
                throw e;
            }
            observateur = obs;
        } catch (RemoteException e) {
            throw traduire(e);
        }
    }

    public synchronized void seDesabonner() throws ErreurCabinet {
        if (observateur == null) {
            throw new ErreurCabinet("Vous n'etes pas abonne aux alertes.");
        }
        ObservateurAlertes obs = observateur;
        observateur = null;
        try {
            cabinet.seDesabonner(obs);
        } catch (RemoteException e) {
            throw traduire(e);
        } finally {
            desexporter(obs);
        }
    }

    // Arret propre : ne leve jamais d'exception (le serveur est peut-etre deja arrete).
    public synchronized void fermer() {
        if (observateur != null) {
            try {
                seDesabonner();
            } catch (ErreurCabinet e) {
                // rien a faire
            }
        }
    }

    private static void desexporter(ObservateurAlertes obs) {
        try {
            UnicastRemoteObject.unexportObject(obs, true);
        } catch (NoSuchObjectException e) {
            // deja desexporte
        }
    }




    private IDossierSuivi dossierDe(String nom) throws ErreurCabinet, RemoteException {
        IAnimal a = cabinet.rechercherPatient(nom);
        if (a == null) {
            throw new ErreurCabinet("Patient introuvable : " + nom);
        }
        return a.getDossier();
    }

    private String decrire(IAnimal a) throws RemoteException {
        return a.getName() + " (" + a.getEspece().getNom() + "), maitre : " + a.getProp()
                + ", race : " + a.getRace();
    }

    private static ErreurCabinet traduire(RemoteException e) {
        if (e instanceof ConnectException || e instanceof ConnectIOException) {
            return new ErreurCabinet("Serveur injoignable. Verifiez qu'il est demarre.");
        }
        return new ErreurCabinet("Erreur de communication avec le serveur : " + e.getMessage());
    }
}