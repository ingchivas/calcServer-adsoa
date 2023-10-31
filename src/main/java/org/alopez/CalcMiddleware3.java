package org.alopez;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CalcMiddleware3 {
    private Map<String, ServerInfo> serverPorts;
    private Map<String, ServerInfo> otherMiddlewares;
    private List<ClientHandler> clientHandlers;
    private ConcurrentHashMap<String, Integer> seenMessages;
    private final String nodeId = UUID.randomUUID().toString();

    public CalcMiddleware3(Map<String, ServerInfo> serverPorts, Map<String, ServerInfo> otherMiddlewares) {
        this.serverPorts = serverPorts;
        this.otherMiddlewares = otherMiddlewares;
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.seenMessages = new ConcurrentHashMap<>();
    }

    public static class ServerInfo {
        public final String host;
        public final int port;

        public ServerInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public void start(int port) {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Middleware with NodeID " + nodeId + " started on port " + port);
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected to Middleware with NodeID " + nodeId + ": " + clientSocket.getInetAddress());

                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clientHandler.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }


    public void initializeMiddleware(int[] availablePorts) {
        int startedPort = -1;

        for (int port : availablePorts) {
            start(port);
            System.out.println("Middleware started successfully on port " + port);
            startedPort = port;
            break;
        }

        if (startedPort != -1) {
            // Populate otherMiddlewares with availablePorts excluding the startedPort
            for (int port : availablePorts) {
                if (port != startedPort) {
                    otherMiddlewares.put("middleware" + port, new ServerInfo("localhost", port));
                }
            }

            // Debug message to display known other middlewares
            System.out.println("[DEBUG] Middleware with NodeID " + nodeId + " knows about the following other middlewares: " + otherMiddlewares);
        } else {
            System.out.println("Failed to start the middleware on any available port.");
        }
    }

    // New ClientHandler integrated
    class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String rawExpression = in.readLine();
                String originatingMiddleware = null;  // The middleware that first received the client's request

                if (rawExpression.contains("->")) {
                    String[] parts = rawExpression.split("->", 2);
                    originatingMiddleware = parts[0];
                    rawExpression = parts[1];
                } else {
                    originatingMiddleware = nodeId;  // This middleware is the originator
                }

                String[] operands = rawExpression.split(" ");
                String operation = operands[1];
                int[] operationPorts;
                switch (operation) {
                    case "+":
                        operationPorts = new int[]{6970, 6975, 6976, 6977};
                        break;
                    case "-":
                        operationPorts = new int[]{6971, 6978, 6979, 6980};
                        break;
                    case "*":
                        operationPorts = new int[]{6972, 6981, 6982, 6983};
                        break;
                    case "/":
                        operationPorts = new int[]{6973, 6984, 6985, 6986};
                        break;
                    default:
                        out.println("Invalid operation");
                        return;
                }

                String localResult = null;
                for (int port : operationPorts) {
                    localResult = forwardRequestToOperationServer(rawExpression, port);
                    if (localResult != null && !localResult.equals("Error forwarding request")) {
                        break;
                    }
                }

                if (nodeId.equals(originatingMiddleware)) {
                    // This middleware is the original receiver of the client's request
                    Map<String, String> aggregatedResults = new HashMap<>();
                    aggregatedResults.put(nodeId, localResult);

                    Map<String, String> otherMiddlewareResults = forwardRequestToOtherMiddlewares(nodeId + "->" + rawExpression);

                    aggregatedResults.putAll(otherMiddlewareResults);

                    out.println(aggregatedResults.toString());  // Sending back aggregated results to the client
                } else {
                    // For other middlewares, just forward the result to the originator
                    out.println(nodeId + "=" + localResult);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String forwardRequestToOperationServer(String expression, int port) {
            try (Socket socket = new Socket("127.0.0.1", port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(expression);
                return in.readLine();
            } catch (ConnectException ce) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error forwarding request";
            }
        }

        private Map<String, String> forwardRequestToOtherMiddlewares(String expression) {
            Map<String, String> results = new HashMap<>();
            for (ServerInfo middleware : otherMiddlewares.values()) {
                try (Socket socket = new Socket(middleware.host, middleware.port);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    System.out.println("[DEBUG] Forwarding request from NodeID " + nodeId + " to Middleware at " + middleware.host + ":" + middleware.port);
                    out.println(expression);
                    String result = in.readLine();
                    if (result != null) {
                        System.out.println("[DEBUG] Received result from Middleware at " + middleware.host + ":" + middleware.port + " - Result: " + result);
                        String[] middlewareResults = result.split(",");
                        for (String res : middlewareResults) {
                            String[] parts = res.split("=");
                            if (parts.length == 2) {
                                results.put(parts[0], parts[1]);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[DEBUG] Error forwarding request to Middleware at " + middleware.host + ":" + middleware.port);
                    e.printStackTrace();
                }
            }
            return results;
        }

    }


    public static void main(String[] args) {
        Map<String, ServerInfo> serverPorts = new HashMap<>();
        serverPorts.put("additionServer", new ServerInfo("localhost", 6976));
        serverPorts.put("subtractionServer", new ServerInfo("localhost", 6979));
        serverPorts.put("multiplicationServer", new ServerInfo("localhost", 6982));
        serverPorts.put("divisionServer", new ServerInfo("localhost", 6985));

        Map<String, ServerInfo> otherMiddlewares = new HashMap<>();
        CalcMiddleware3 middleware = new CalcMiddleware3(serverPorts, otherMiddlewares);
        int[] availablePorts = {6421, 6969, 6420};

        middleware.initializeMiddleware(availablePorts);


    }
}

