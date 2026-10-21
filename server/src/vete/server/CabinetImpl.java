package vete.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import vete.common.Espece;
import vete.common.IAnimal;//A4
import vete.common.ICabinet;
import vete.common.IObservateurCabinet; //A6


//etape A6 j'ai rancotré un peu diffuculte pour consturir le code , j'ai utiliser ai reformé et adapté porposition

public class CabinetImpl extends UnicastRemoteObject implements ICabinet {

    private static final int[] SEUILS = {100, 500, 1000};

    private final List<IAnimal> patients = new ArrayList<>();
    private final List<IObservateurCabinet> observateurs = new ArrayList<>();

    public CabinetImpl() throws RemoteException {
        super();
    }

    // locale metod 
    public synchronized void ajouterPatient(IAnimal animal) {
        patients.add(animal);
    }

    @Override
    public synchronized List<IAnimal> getPatients() throws RemoteException {
        return new ArrayList<>(patients);
    }

    @Override
    public synchronized IAnimal rechercherPatient(String nom) throws RemoteException {
        for (IAnimal a : patients) {
            if (a.getName().equals(nom)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public IAnimal enregistrerPatient(String nom, String maitre, String race, Espece espece)
            throws RemoteException {
        AnimalImpl animal = new AnimalImpl(nom, maitre, race, espece);
        int avant;
        int apres;
        synchronized (this) {
            avant = patients.size();
            patients.add(animal);
            apres = patients.size();
        }
        verifierSeuils(avant, apres);
        return animal;
    }

    @Override
    public boolean retirerPatient(String nom) throws RemoteException {
        boolean retire = false;
        int avant;
        int apres;
        synchronized (this) {
            avant = patients.size();
            for (int i = 0; i < patients.size(); i++) {
                if (patients.get(i).getName().equals(nom)) {
                    patients.remove(i);
                    retire = true;
                    break;
                }
            }
            apres = patients.size();
        }
        if (retire) {
            verifierSeuils(avant, apres);
        }
        return retire;
    }

    @Override
    public void sabonner(IObservateurCabinet observateur) throws RemoteException {
        int total;
        synchronized (observateurs) {
            observateurs.add(observateur);
            total = observateurs.size();
        }
        System.out.println("[cabinet] nouvel abonne (total : " + total + ")");
    }

    @Override
    public void seDesabonner(IObservateurCabinet observateur) throws RemoteException {
        int total;
        synchronized (observateurs) {
            observateurs.remove(observateur);
            total = observateurs.size();
        }
        System.out.println("[cabinet] desabonnement (total : " + total + ")");
    }

    // comparer avant et apres les nombre de client
    // si les deux valeurs sont de part et d'autre du seuil.
    private void verifierSeuils(int avant, int apres) {
        for (int seuil : SEUILS) {
            if (avant < seuil && apres >= seuil) {
                notifierTous(seuil, apres, true);
            } else if (avant >= seuil && apres < seuil) {
                notifierTous(seuil, apres, false);
            }
        }
    }

    private void notifierTous(int seuil, int nombre, boolean aLaHausse) {
        List<IObservateurCabinet> copie;
        synchronized (observateurs) {
            copie = new ArrayList<>(observateurs);
        }
        for (IObservateurCabinet o : copie) {
            try {
                o.notifier(seuil, nombre, aLaHausse);
            } catch (RemoteException e) {
                System.out.println("[cabinet] observateur injoignable, retire : " + e.getClass().getSimpleName());
                synchronized (observateurs) {
                    observateurs.remove(o);
                }
            }
        }
    }
}