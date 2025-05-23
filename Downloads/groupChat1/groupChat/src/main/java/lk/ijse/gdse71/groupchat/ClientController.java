package lk.ijse.gdse71.groupchat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * This class controls the chat client window.
 * It handles connecting to the server, sending messages, and displaying received messages.
 */
public class ClientController implements Initializable {

    // These are the UI elements from our FXML file
    @FXML
    private Button btnSendClient;
    @FXML
    private TextArea txtAreaClientChatDisplay;
    @FXML
    private TextField txtFieldClientMessage;
    @FXML
    private Label lblClientName;
    @FXML
    private VBox chatPane;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String clientName;

    /**
     * Sets the client's name and updates the display
     * @param name The name to set for this client
     */
    public void setClientName(String name) {
        this.clientName = name;
        Platform.runLater(() -> lblClientName.setText(name));
    }

    /**
     * This method runs when the chat window opens
     * It connects to the server and starts listening for messages
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Start a new thread to handle server communication
        new Thread(() -> {
            try {
                // Connect to the server
                socket = new Socket("localhost", 5000);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                // Keep listening for messages
                while (true) {
                    String message = dataInputStream.readUTF();

                    // Handle different types of messages
                    if (message.endsWith(": Date")) {
                        // Show current date
                        Platform.runLater(() -> {
                            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            Label messageLabel = new Label("Current Date: " + currentDate);
                            messageLabel.setFont(new Font(14));
                            chatPane.getChildren().add(messageLabel);
                        });
                    } else if (message.endsWith(": Time")) {
                        // Show current time
                        Platform.runLater(() -> {
                            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            Label messageLabel = new Label("Current Time: " + currentTime);
                            messageLabel.setFont(new Font(14));
                            chatPane.getChildren().add(messageLabel);
                        });
                    } else if (message.startsWith("UPTIME:")) {
                        // Show server uPPtime
                        Platform.runLater(() -> {
                            String uptimeSeconds = message.substring(7);
                            Label messageLabel = new Label("Server Uptime: " + uptimeSeconds + " seconds");
                            messageLabel.setFont(new Font(14));
                            chatPane.getChildren().add(messageLabel);
                        });
                    } else {
                        // Show  chat mesage
                        Platform.runLater(() -> {
                            Label messageLabel = new Label(message);
                            messageLabel.setFont(new Font(14));
                            chatPane.getChildren().add(messageLabel);
                        });
                    }
                }
            } catch (IOException e) {
                // Show error message if connection is lost
                Platform.runLater(() -> {
                    Label messageLabel = new Label("Connection closed by server");
                    messageLabel.setFont(new Font(14));
                    chatPane.getChildren().add(messageLabel);
                    txtFieldClientMessage.setDisable(true);
                    btnSendClient.setDisable(true);
                });
            }
        }).start();
    }

    /**
     * This method is called when the sen button is clicked
     * It sends the message to the server
     */
    @FXML
    void btnSendOnAction() {
        String sendingMessage = txtFieldClientMessage.getText().trim();
        if (!sendingMessage.isEmpty()) {
            try {
                // Send the message to the server
                dataOutputStream.writeUTF(sendingMessage);
                dataOutputStream.flush();
                txtFieldClientMessage.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
