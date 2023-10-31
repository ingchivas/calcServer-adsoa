package org.alopez;

import java.io.*;
import java.net.*;

public class CalcTestClient {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private EncoderDecoder encoderDecoder;

    public CalcTestClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.encoderDecoder = new EncoderDecoder();
    }

    public void startConnection() throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public String sendMessage(byte[] encodedMessage) throws IOException {
        out.writeInt(encodedMessage.length);  // First send the length of the encoded message
        out.write(encodedMessage);

        // Read the length of the incoming encoded message
        int responseLength = in.readInt();
        byte[] responseBytes = new byte[responseLength];
        in.readFully(responseBytes);

        return encoderDecoder.decode(responseBytes);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public static void main(String[] args) {
        CalcTestClient client = new CalcTestClient("localhost", 6969);
        try {
            client.startConnection();

            String expression = "2 + 2";

            byte[] encodedMessage = client.encoderDecoder.encodeOperation(expression);
            String response = client.sendMessage(encodedMessage);

            System.out.println("Response from server: " + response);

            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
