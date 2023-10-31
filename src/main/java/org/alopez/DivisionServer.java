package org.alopez;

public class DivisionServer extends OperationServer {
    @Override
    protected double performOperation(double operand1, double operand2) {
        if (operand2 == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return operand1 / operand2;
    }

    public static void main(String[] args) {
        OperationServer server = new DivisionServer();
        int[] ports = {6973, 6984, 6985, 6986};
        server.startOnAvailablePort(ports);
    }
}
