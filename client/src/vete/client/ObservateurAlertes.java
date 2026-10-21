package vete.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

import vete.common.IObservateurCabinet;

public class ObservateurAlertes extends UnicastRemoteObject implements IObservateurCabinet {

    private final Consumer<String> afficheur;

    public ObservateurAlertes(Consumer<String> afficheur) throws RemoteException {
        super();
        this.afficheur = afficheur;
    }

    @Override
    public void notifier(int seuil, int nombrePatients, boolean aLaHausse) throws RemoteException {
        String sens = aLaHausse ? "a la hausse" : "a la baisse";
        afficheur.accept("[ALERTE] seuil " + seuil + " franchi " + sens
                + " : " + nombrePatients + " patients");
    }
}