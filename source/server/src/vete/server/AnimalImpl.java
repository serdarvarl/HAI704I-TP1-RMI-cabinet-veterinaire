package vete.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import vete.common.IAnimal;
//ajouter class 
import vete.common.Espece;

// ajouter class dossier suiv
import vete.common.IDossierSuivi;


public class AnimalImpl extends UnicastRemoteObject implements IAnimal {
    private String name;
    private String prop;
    private String race;
    //private String espece;
    private Espece espece;

    //A3
    private DossierSuiviImpl dossier;



    //public AnimalImpl(String name, String prop, String race ,String espece)  throws RemoteException {
    public AnimalImpl(String name, String prop, String race ,Espece espece)  throws RemoteException {

        super();
        this.name = name;
        this.prop = prop;
        this.race = race;
        this.espece = espece;
        //A3
        this.dossier = new DossierSuiviImpl();
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getProp() throws RemoteException {
        return prop;
    }

    @Override
    public String getRace() throws RemoteException {
        return race;
    }

    @Override
    //public String getEspece() throws RemoteException { A2
    public Espece getEspece() throws RemoteException {
        System.out.println("[serveur] identityHashCode = " + System.identityHashCode(espece));
        return espece;
    }

    //ajouter nouveo method
    @Override
    public IDossierSuivi getDossier() throws RemoteException {
    return dossier;
    }




}