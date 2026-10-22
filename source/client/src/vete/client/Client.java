package vete.client;

import java.lang.reflect.Proxy;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import vete.common.Espece; //A5 
import vete.common.IAnimal;
import vete.common.ICabinet;

// changer le client.java  à pd A4 maintenant cherher dans la cabinet 

public class Client {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            System.out.println("Liaisons dans le registre : " + Arrays.toString(registry.list()));

            ICabinet cabinet = (ICabinet) registry.lookup("cabinet");// cherher 

            List<IAnimal> patients = cabinet.getPatients(); //methode
            System.out.println("Nombre de patients : " + patients.size());

            //for cherher list
            for (IAnimal p : patients) { 
                System.out.println(" - " + p.getName() + " (" + p.getEspece().getNom() + ")"
                        + " proxy ? " + Proxy.isProxyClass(p.getClass())); //stub
            }

            IAnimal trouve = cabinet.rechercherPatient("Moon"); // methode
            System.out.println("Recherche 'Moon' : "
                    + (trouve != null ? trouve.getName() + ", maitre " + trouve.getProp() : "introuvable"));

            IAnimal absent = cabinet.rechercherPatient("Inconnu"); 
            System.out.println("Recherche 'Inconnu' : "
                    + (absent != null ? absent.getName() : "introuvable"));
            
                    
            //A5 creer un patient depuis client
            int avant = cabinet.getPatients().size();
            System.out.println("Patients avant ajout : " + avant);

            IAnimal nouveau = cabinet.enregistrerPatient("Rex", "Paul", "Berger allemand",
                    new Espece("chien", 13));
            System.out.println("Patient cree : " + nouveau.getName()
                    + " (proxy ? " + Proxy.isProxyClass(nouveau.getClass()) + ")");

            int apres = cabinet.getPatients().size();
            System.out.println("Patients apres ajout : " + apres);

            IAnimal retrouve = cabinet.rechercherPatient("Rex");
            System.out.println("Recherche 'Rex' : "
                    + (retrouve != null ? retrouve.getName() + ", maitre " + retrouve.getProp() : "introuvable"));        

        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }
}