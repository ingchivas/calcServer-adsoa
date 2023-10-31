package org.alopez;

public class MultiplicationServer extends OperationServer {
    @Override
    protected double performOperation(double operand1, double operand2) {
        return operand1 * operand2;
    }

    public static void main(String[] args) {
        OperationServer server = new MultiplicationServer();
        int[] ports = {6972, 6981, 6982, 6983};
        server.startOnAvailablePort(ports);
    }
}
