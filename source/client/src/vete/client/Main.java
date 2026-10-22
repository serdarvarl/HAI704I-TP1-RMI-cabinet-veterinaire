package vete.client;

public class Main {

    public static void main(String[] args) {
        String hote = (args.length > 0) ? args[0] : "localhost";
        int port = 1099;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide : " + args[1]);
                return;
            }
        }
        try {
            ClientCabinet logique = new ClientCabinet(hote, port);
            new ConsoleUI(logique).lancer();
        } catch (ErreurCabinet e) {
            System.err.println("Impossible de demarrer : " + e.getMessage());
        }
    }
}
