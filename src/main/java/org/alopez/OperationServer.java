package org.alopez;

import java.net.*;
import java.io.*;
import java.util.Arrays;

public abstract class OperationServer {
    private ServerSocket serverSocket;

    private EncoderDecoder encoderDecoder = new EncoderDecoder();


    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Operation server started on 127.0.0.1:" + port);
        EncoderDecoder encoderDecoder = new EncoderDecoder();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String expression = in.readLine();
//                    String acknowledgementEvent = "Message received";
//                    byte[] acknowledgementMessage = encoderDecoder.encodeAcknowledgement(acknowledgementEvent);
//                    out.write(Arrays.toString(acknowledgementMessage));



                    String[] operands = expression.split(" ");
                    double operand1 = Double.parseDouble(operands[0]);
                    double operand2 = Double.parseDouble(operands[2]);
                    double result = performOperation(operand1, operand2);

                    out.println(result);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void startOnAvailablePort(int[] ports) {
        for (int port : ports) {
            try {
                start(port);
                break;
            } catch (BindException e) {
                System.out.println("Port " + port + " is already in use.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract double performOperation(double operand1, double operand2);
}
