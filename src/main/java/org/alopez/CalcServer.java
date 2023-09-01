package org.alopez;
import java.net.*;
import java.io.*;

public class CalcServer {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on 127.0.0.1:" + port);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread to handle the client's request
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        CalcServer server = new CalcServer();
        int[] ports = {6970, 6971, 6972, 6973, 6974};
        try {
            // Try to start the server on each port until one is available
            for (int port : ports) {
                try {
                    server.start(port);
                    break;
                } catch (BindException e) {
                    System.out.println("Port " + port + " is already in use.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        clientSocket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String expression = in.readLine();
            String[] operands = expression.split(" ");
            System.out.println("Received expression: " + expression);
            System.out.printf("Operand 1: %s, Operand 2: %s, Operator: %s%n", operands[0], operands[2], operands[1]);
            System.out.println("Evaluating...");

            double operand1 = Double.parseDouble(operands[0]);
            double operand2 = Double.parseDouble(operands[2]);
            double result = 0;

            switch (operands[1]) {
                case "+":
                    result = operand1 + operand2;
                    break;
                case "-":
                    result = operand1 - operand2;
                    break;
                case "*":
                    result = operand1 * operand2;
                    break;
                case "/":
                    result = operand1 / operand2;
                    break;
            }
            System.out.println("Sending result: " + result);

            out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
