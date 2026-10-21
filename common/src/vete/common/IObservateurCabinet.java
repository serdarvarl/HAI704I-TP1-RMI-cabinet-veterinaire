package vete.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IObservateurCabinet extends Remote {

    // seuil : 100, 500 ou 1000 ; aLaHausse : true si le seuil est franchi vers le haut
    void notifier(int seuil, int nombrePatients, boolean aLaHausse) throws RemoteException;
}