import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Server extends Thread {
    // Mappa che associa ad ogni client connesso il suo nickname
    // Map that associates each connected client with its nickname
    private Map<Socket, String> clientNicknames = new HashMap<>();
    // Lista dei nickname già utilizzati dai client connessi al server
    // List of nicknames already used by clients connected to the server
    private List<String> usedNicknames = new ArrayList<>();

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8000)) {
            // Accetta le connessioni dei client in modo iterativo
             // Crea un oggetto della classe ChatClient
             // Accept client connections iteratively
              // Create an object of class ChatClient
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Avvia un nuovo thread per gestire le interazioni del client
                // Start a new thread to handle client interactions
                new ClientThread(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientThread extends Thread {
        // Aggiungi la variabile di istanza nickname qui
        // Add the nickname instance variable here
        private String nickname;
        private Socket clientSocket;
        private int numInteractions = 0;
    
        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintStream out = new PrintStream(clientSocket.getOutputStream())) {
                // Ricevi il nickname del client
                // Get the client's nickname
                nickname = in.readLine();
                // Controlla se il nickname è già stato utilizzato da un altro client
                // Check if the nickname has already been used by another client
                if (usedNicknames.contains(nickname)) {
                    // Invia un messaggio di errore al client e interrompi il thread
                    // Send an error message to the client and kill the thread
                    out.println("Nickname già in uso");
                    return;
                }
                // Aggiungi il nickname del client alla lista dei nickname utilizzati
                // Add the client's nickname to the list of used nicknames
                usedNicknames.add(nickname);
                // Aggiungi il nickname del client alla mappa
                // Add the client's nickname to the map
                clientNicknames.put(clientSocket, nickname);
                // Invia a tutti i client connessi il messaggio di connessione del client
                // Send the client connection message to all connected clients
                broadcast(nickname + " si e`connesso al server.");
                   // Ricevi e invia i messaggi in modo iterativo finché il client è connesso
                   // Receive and send messages iteratively as long as the client is connected
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                numInteractions++;
                // Invia a tutti i client connessi il messaggio del client anteponendo il suo nickname
                // Send the client's message to all connected clients prepending the client's nickname
                broadcast(nickname + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Rimuovi il nickname del client dalla lista dei nickname utilizzati
            // Remove the client's nickname from the list of used nicknames
            usedNicknames.remove(nickname);
            // Rimuovi il nickname del client dalla mappa
            // Remove the client's nickname from the map
            clientNicknames.remove(clientSocket);
            // Invia a tutti i client connessi il messaggio di disconnessione del client
            // Send the client disconnect message to all connected clients
            broadcast(nickname + " si è disconnesso dal server.");
            // Stampa a video le iterazioni del client
            // Print the iterations of the client
            System.out.println(nickname + " ha effettuato " + numInteractions + " iterazioni prima di disconnettersi.");
        }
    }

    // Invia il messaggio a tutti i client connessi
    // Send the message to all connected clients
    private void broadcast(String message) {
        for (Map.Entry<Socket, String> entry : clientNicknames.entrySet()) {
            Socket clientSocket = entry.getKey();
            try {
                PrintStream out = new PrintStream(clientSocket.getOutputStream());
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public static void main(String[] args) {
    new Server().start();
}
}