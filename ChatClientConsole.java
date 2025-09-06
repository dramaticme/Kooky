import java.io.*;
import java.net.*;

public class ChatClientConsole {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to chat server");
            System.out.println("Commands: /nick <name>, /pm <user> <message>");

            // Thread to listen for server messages (decrypt on receive)
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        String decrypted = CryptoUtils.decrypt(msg); 
                        System.out.println(decrypted);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Main thread sends user input (encrypt before sending)
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                String encrypted = CryptoUtils.encrypt(userInput); 
                out.println(encrypted);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
