package vete.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import vete.common.Espece;
import vete.common.ICabinet;

public class ClientRemplisseur {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ICabinet cabinet = (ICabinet) registry.lookup("cabinet");

            int cible = 100;
            int n = cabinet.getPatients().size();
            while (n < cible) {
                n++;
                cabinet.enregistrerPatient("Patient" + n, "Maitre" + n, "Croise",
                        new Espece("chien", 13));
            }
            System.out.println("Patients dans le cabinet : " + cabinet.getPatients().size());

            boolean retire = cabinet.retirerPatient("Patient100");
            System.out.println("Patient100 retire : " + retire);
            System.out.println("Patients dans le cabinet : " + cabinet.getPatients().size());
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }
}