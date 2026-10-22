package vete.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ICabinet extends Remote {

    List<IAnimal> getPatients() throws RemoteException;

    // retouner si null
    IAnimal rechercherPatient(String nom) throws RemoteException;

    IAnimal enregistrerPatient(String nom, String maitre, String race, Espece espece) //A5 ajoueter client
            throws RemoteException;

    //A6 ajouter les methode

    boolean retirerPatient(String nom) throws RemoteException;

    void sabonner(IObservateurCabinet observateur) throws RemoteException;

    void seDesabonner(IObservateurCabinet observateur) throws RemoteException;
}