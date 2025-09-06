import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler extends Thread {
    private static AtomicInteger userCount = new AtomicInteger(0);
    private String nickname;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clientHandlers;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers) {
        this.socket = socket;
        this.clientHandlers = clientHandlers;
        this.nickname = "User" + userCount.incrementAndGet();
    }

    public String getNickname() {
        return nickname;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            broadcast(nickname + " joined the chat!");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/nick ")) {
                    String newNick = message.substring(6).trim();
                    broadcast(nickname + " is now known as " + newNick);
                    this.nickname = newNick;
                } else if (message.startsWith("/pm ")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length >= 3) {
                        String targetNick = parts[1];
                        String privateMsg = parts[2];
                        sendPrivate(targetNick, "(PM from " + nickname + "): " + privateMsg);
                    } else {
                        out.println("Usage: /pm <user> <message>");
                    }
                } else {
                    broadcast(nickname + ": " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void broadcast(String message) {
    String encrypted = CryptoUtils.encrypt(message);
    for (ClientHandler client : clientHandlers) {
        client.out.println(encrypted);
    }
    System.out.println("[LOG] " + message);
}

private void sendPrivate(String targetNick, String message) {
    String encrypted = CryptoUtils.encrypt(message);
    boolean found = false;
    for (ClientHandler client : clientHandlers) {
        if (client.getNickname().equalsIgnoreCase(targetNick)) {
            client.out.println(encrypted);
            found = true;
            break;
        }
    }
    if (!found) {
        out.println(CryptoUtils.encrypt("User " + targetNick + " not found."));
    }
}

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlers.remove(this);
        broadcast(nickname + " left the chat.");
    }
}
