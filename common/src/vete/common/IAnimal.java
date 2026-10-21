package vete.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAnimal extends Remote {
    String getName() throws RemoteException;
    String getProp() throws RemoteException;
    String getRace() throws RemoteException;
    //String getEspece() throws RemoteException;
    Espece getEspece() throws RemoteException; // on a passe A2 espece denvenir une class   

    IDossierSuivi getDossier() throws RemoteException; //A3 
}

