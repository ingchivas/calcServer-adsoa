package org.alopez;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class TestClient {
    public static void main(String[] args) {
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 6969);

            // Create a PrintWriter to send data to the server
            OutputStream out = socket.getOutputStream();

            // Create a BufferedReader to read data from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String operation = "5 * 6";
            String event = "abc";
            EncoderDecoder encoderDecoder = new EncoderDecoder();

            byte[] encodedOperation = encoderDecoder.encodeArithmeticOperation(operation, event);
            System.out.println("Encoded operation: " + Arrays.toString(encodedOperation));
            String decodedOperation = encoderDecoder.decode(encodedOperation);
            System.out.println("Decoded operation: " + decodedOperation);
            String operationString = EncoderDecoder.extractArithmeticOperation(encodedOperation);
            System.out.println("Extracted operation: " + operationString);

            out.write(encodedOperation, 0, encodedOperation.length);

            // Read the response from the server
            String response = in.readLine();
            System.out.println("Response from server: " + response);

            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
