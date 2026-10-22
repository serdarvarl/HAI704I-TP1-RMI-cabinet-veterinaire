

package vete.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import vete.common.Espece; //class espece

public class Server {

    //private static AnimalImpl animal;
    private static CabinetImpl cabinet; // A4 ajouté CabinetImp class, cabinet est une variable

    public static void main(String[] args) {
        try {
            //animal = new AnimalImpl("Moon","Alex","Samoyed","chien");
            cabinet = new CabinetImpl(); // appeler CabinetImp
            //animal = new AnimalImpl("Moon","Alex","Samoyed", new Espece("chien",13)); // class espece prende 2 parametre

            cabinet.ajouterPatient(new AnimalImpl("Moon", "Alex", "Samoyed", new Espece("chien", 13)));
            cabinet.ajouterPatient(new AnimalImpl("Felix", "Marie", "Siamois", new Espece("chat", 15)));

            Registry registry = LocateRegistry.createRegistry(1099);
            //registry.rebind("animal", animal);
            registry.rebind("cabinet", cabinet); //A4

            System.out.println("Server ready");

        } catch (Exception e) {
            System.err.println("Server exception: "+ e);
            e.printStackTrace();
        }
    }


}
