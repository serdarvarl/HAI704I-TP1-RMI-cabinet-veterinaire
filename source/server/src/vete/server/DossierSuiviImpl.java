package vete.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import vete.common.IDossierSuivi;

public class DossierSuiviImpl extends UnicastRemoteObject implements IDossierSuivi {

    private String etatSante = "inconnu";
    private List<String> observations = new ArrayList<>();

    public DossierSuiviImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized String getEtatSante() throws RemoteException {
        return etatSante;
    }

    @Override
    public synchronized void setEtatSante(String etatSante) throws RemoteException {
        this.etatSante = etatSante;
    }

    @Override
    public synchronized void ajouterObservation(String observation) throws RemoteException {
        observations.add(observation);
    }

    @Override
    public synchronized List<String> getObservations() throws RemoteException {
        return new ArrayList<>(observations);
    }
}