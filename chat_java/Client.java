import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintStream out;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;

    public Client() {
        // Crea la finestra // Create the window
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crea il pannello per i messaggi // Create the message board
        messageArea = new JTextArea(8, 40);
        messageArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Crea il pannello per l'invio dei messaggi // Create the posting panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        messageField = new JTextField(40);
        inputPanel.add(messageField, BorderLayout.CENTER);
        sendButton = new JButton("Invia");
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        // Mostra la finestra // Show the window
        frame.pack();
        frame.setVisible(true);

        // Chiedi al client di inserire il suo nickname // Ask the client to enter his nickname
        String nickname = JOptionPane.showInputDialog(frame, "Inserisci il tuo nickname:", "Nickname", JOptionPane.PLAIN_MESSAGE);

        // Crea il thread per leggere i messaggi dal server in modo asincrono // Create the thread to read messages from the server asynchronously
        new Thread(() -> {
            try {
                // Connetti al server // Connect to the server
                socket = new Socket("localhost", 8000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintStream(socket.getOutputStream());
                // Invia il nickname al server // Send the nickname to the server
                out.println(nickname);
                // Ricevi la risposta del server // Get the response from the server
                    String response = in.readLine();
                    if (response.equals("Nickname già in uso")) {
                        // Se il nickname è già in uso, chiedi al client di inserirne uno nuovo
                        // If the nickname is already in use, ask the client to enter a new one
                        JOptionPane.showMessageDialog(frame, "Il nickname inserito è già in uso. Scegline un altro.", "Errore", JOptionPane.ERROR_MESSAGE);
                        // Fai tornare indietro il client all'inserimento del nickname
                        // Make the client go back on entering the nickname
                        frame.dispose();
                        new Client();
                    } else {
                     // Altrimenti, ricevi e stampa i messaggi in modo iterativo
                     // Otherwise, receive and print messages iteratively
                    while (true) {
                        String message = in.readLine();
                        if (message == null) {
                            break;
                        }
                        messageArea.append(message + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Aggiungi un listener al pulsante Invia per inviare i messaggi al server
        // Add a listener to the Submit button to send messages to the server
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Invia il messaggio al server
                // Send the message to the server
                out.println(messageField.getText());
                // Svuota il campo di testo
                // Clear the text field
                messageField.setText("");
            }
        });
    }

    public static void main(String[] args) {
        new Client();
    }
}