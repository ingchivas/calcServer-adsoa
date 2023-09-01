package org.alopez;
import java.net.*;
import java.io.*;

public class CalcTestClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String response = in.readLine();
        return response;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        CalcTestClient client = new CalcTestClient();
        try {
            client.startConnection("127.0.0.1", 6969);
            String response = client.sendMessage("91.1 + 2.1");
            System.out.println(response);
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
