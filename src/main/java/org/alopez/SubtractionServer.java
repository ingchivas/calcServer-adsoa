package org.alopez;

public class SubtractionServer extends OperationServer {
    @Override
    protected double performOperation(double operand1, double operand2) {
        return operand1 - operand2;
    }

    public static void main(String[] args) {
        OperationServer server = new SubtractionServer();
        int[] ports = {6971, 6978, 6979, 6980};
        server.startOnAvailablePort(ports);
    }
}
