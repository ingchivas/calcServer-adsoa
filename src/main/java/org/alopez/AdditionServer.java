package org.alopez;

public class AdditionServer extends OperationServer {
    @Override
    protected double performOperation(double operand1, double operand2) {
        // Console output for debugging
        System.out.println("AdditionServer Recieved: " + operand1 + " + " + operand2 + " = " + (operand1 + operand2));
        return operand1 + operand2;
    }

    public static void main(String[] args) {
        OperationServer server = new AdditionServer();
        int[] ports = {6970, 6975, 6976, 6977};
        server.startOnAvailablePort(ports);
    }
}
