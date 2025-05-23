package lk.ijse.gdse71.groupchat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gdse71.groupchat.Client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class controls the chat server window.
 * It manages all client connections and handles message broadcasting between clients.
 */
public class ServerController implements Initializable {

    @FXML
    private Button btnSendServer;
    @FXML
    private TextArea txtAreaServerChatDisplay;
    @FXML
    private TextField txtClientName;

    private ArrayList<Client> clients = new ArrayList<>(); // List of all connected clients
    private ServerSocket serverSocket;
    private long serverStartTime;
    /**
     * This method runs when the server window opens.
     * It starts the server and begins listening for client connections.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Record when server started
        serverStartTime = System.currentTimeMillis();
        
        // Start a new thread to handle client connections
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                System.out.println("Server started");
                
                while (true) {
                    Socket socket = serverSocket.accept();
                    
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    Client client = new Client();
                    client.setDataInputStream(dataInputStream);
                    client.setDataOutputStream(dataOutputStream);
                    client.setSocket(socket);
                    clients.add(client);

                    handleClient(client);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles all communication with a connected client.
     * Processes messages and special commands from the client.
     * @param client The client to handle
     */
    private void handleClient(Client client) {
        new Thread(() -> {
            try {
                DataInputStream dataInputStream = client.getDataInputStream();
                while (true) {
                    String message = dataInputStream.readUTF();

                    if (message.equalsIgnoreCase("Date")) {
                        broadcast(client.getClientName() + ": Date", client);
                    } else if (message.equalsIgnoreCase("Time")) {
                        broadcast(client.getClientName() + ": Time", client);
                    } else if (message.equalsIgnoreCase("Uptime")) {
                        long uptimeSeconds = (System.currentTimeMillis() - serverStartTime) / 1000;
                        broadcast("UPTIME:" + uptimeSeconds, client);
                    } else if (message.equalsIgnoreCase("BYE")) {
                        broadcast("System: " + client.getClientName() + " has left the chat", client);
                        clients.remove(client);
                        client.getSocket().close();
                        break;
                    } else {
                        // Regular chat message
                        broadcast(client.getClientName() + ": " + message, client);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Sends a message to all connected clients except the sender.
     * @param message The message to send
     * @param sender The client who sent the message (null for system messages)
     */
    private void broadcast(String message, Client sender) {
        synchronized (clients) {
            for (Client client : clients) {
                if (client != sender) {
                    try {
                        client.getDataOutputStream().writeUTF(message);
                        client.getDataOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * This method is called when the "Add Client" button is clicked.
     * It creates a new client window and connects it to the server.
     */
   

    public void btnAddNewClientONAction(ActionEvent actionEvent) throws IOException {
          String clientName = txtClientName.getText().trim();
        if (!clientName.isEmpty()) {
            // Show in server display
            txtAreaServerChatDisplay.appendText(clientName + " joined the chat\n");

            // Create new client window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Client.fxml"));
            Parent parent = loader.load();
            ClientController clientController = loader.getController();
            clientController.setClientName(clientName);

            // Show the client window
            Stage stage = new Stage();
            stage.setTitle(clientName);
            stage.setScene(new Scene(parent));
            stage.show();
            txtClientName.clear();
        }
    }
}
