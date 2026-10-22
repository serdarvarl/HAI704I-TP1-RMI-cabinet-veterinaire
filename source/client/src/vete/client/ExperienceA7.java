package vete.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import vete.common.Chien; //A7
import vete.common.ICabinet;

public class ExperienceA7 {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ICabinet cabinet = (ICabinet) registry.lookup("cabinet");

            cabinet.enregistrerPatient("Rex2", "Paul", "Berger allemand", new Chien());
            System.out.println("Patient ajoute.");
        } catch (Exception e) {
            // On remonte toute la chaine des causes, de l'exterieur vers l'interieur.
            Throwable t = e;
            while (t != null) {
                System.err.println("-> " + t.getClass().getName() + " : " + t.getMessage());
                t = t.getCause();
            }
        }
    }
}