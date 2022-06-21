import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserProperties implements Runnable {

    public static ArrayList<UserProperties> userProperties = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public UserProperties(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            userProperties.add(this);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            broadcastMessage( format.format(new Date()) + " " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (UserProperties userProperties : UserProperties.userProperties) {
            try {
                if (!userProperties.clientUsername.equals(clientUsername)) {
                    userProperties.bufferedWriter.write(messageToSend);
                    userProperties.bufferedWriter.newLine();
                    userProperties.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        userProperties.remove(this);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        broadcastMessage(format.format(new Date()) + " " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
