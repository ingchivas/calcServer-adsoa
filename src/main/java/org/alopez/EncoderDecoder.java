package org.alopez;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EncoderDecoder {
    private static final short ADDITION_CODE = 1;
    private static final short SUBTRACTION_CODE = 2;
    private static final short MULTIPLICATION_CODE = 3;
    private static final short DIVISION_CODE = 4;
    private static final short RESULT_CODE = 5;
    private static final short ACKNOWLEDGEMENT_CODE = 100;
    private static final int OPERATION_TYPE_SIZE = 2;
    private static final int INFO_SIZE = 2;
    private static final int NUMBER_SIZE = 4;

    public byte[] encodeArithmeticOperation(short operationType, String event, float numberOne, float numberTwo) {
        if (operationType < ADDITION_CODE || operationType > DIVISION_CODE) {
            throw new IllegalArgumentException("Invalid operation type for arithmetic operation: " + operationType);
        }
        byte[] eventBytes = event.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(OPERATION_TYPE_SIZE + INFO_SIZE + eventBytes.length + 2 * NUMBER_SIZE);
        buffer.putShort(operationType);
        buffer.putShort((short) eventBytes.length);
        buffer.put(eventBytes);
        buffer.putFloat(numberOne);  // putFloat() is used instead of putInt() because the numbers are floats
        buffer.putFloat(numberTwo);
        return buffer.array();
    }

    public byte[] encodeArithmeticOperation(String operation, String event) {
        String[] parts = operation.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid operation format: " + operation);
        }
        float numberOne = Float.parseFloat(parts[0]);
        float numberTwo = Float.parseFloat(parts[2]);
        short operationType;
        switch (parts[1]) {
            case "+":
                operationType = ADDITION_CODE;
                break;
            case "-":
                operationType = SUBTRACTION_CODE;
                break;
            case "*":
                operationType = MULTIPLICATION_CODE;
                break;
            case "/":
                operationType = DIVISION_CODE;
                break;
            default:
                throw new IllegalArgumentException("Invalid operation: " + parts[1]);
        }
        return encodeArithmeticOperation(operationType, event, numberOne, numberTwo);
    }


    public byte[] encodeAcknowledgement(String event) {
        byte[] eventBytes = event.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(OPERATION_TYPE_SIZE + INFO_SIZE + eventBytes.length);
        buffer.putShort(ACKNOWLEDGEMENT_CODE);
        buffer.putShort((short) eventBytes.length);
        buffer.put(eventBytes);
        return buffer.array();
    }

    public byte[] encodeResult(String event, int result) {
        byte[] eventBytes = event.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(OPERATION_TYPE_SIZE + INFO_SIZE + eventBytes.length + NUMBER_SIZE);
        buffer.putShort(RESULT_CODE);
        buffer.putShort((short) eventBytes.length);
        buffer.put(eventBytes);
        buffer.putInt(result);
        return buffer.array();
    }

    public static String extractArithmeticOperation(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        short operationType = buffer.getShort();
        if (operationType < ADDITION_CODE || operationType > DIVISION_CODE) {
            throw new IllegalArgumentException("Invalid operation type for arithmetic operation: " + operationType);
        }
        short eventSize = buffer.getShort();  // get the event size
        buffer.position(buffer.position() + eventSize);  // skip over the event bytes
        float numberOne = buffer.getFloat();
        float numberTwo = buffer.getFloat();
        String operator;
        switch (operationType) {
            case ADDITION_CODE:
                operator = "+";
                break;
            case SUBTRACTION_CODE:
                operator = "-";
                break;
            case MULTIPLICATION_CODE:
                operator = "*";
                break;
            case DIVISION_CODE:
                operator = "/";
                break;
            default:
                throw new IllegalArgumentException("Invalid operation type: " + operationType);
        }
        return numberOne + " " + operator + " " + numberTwo;
    }

    public String decode(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        short operationType = buffer.getShort();
        short eventSize = buffer.getShort();
        byte[] eventBytes = new byte[eventSize];
        buffer.get(eventBytes);
        String event = new String(eventBytes, StandardCharsets.UTF_8);

        if (operationType >= ADDITION_CODE && operationType <= DIVISION_CODE) {
            float numberOne = buffer.getFloat();
            float numberTwo = buffer.getFloat();
            return operationType + "|" + event + "|" + numberOne + "|" + numberTwo;
        } else if (operationType == ACKNOWLEDGEMENT_CODE) {
            return operationType + "|" + event;
        } else if (operationType == RESULT_CODE) {
            int result = buffer.getInt();
            return operationType + "|" + event + "|" + result;
        } else {
            throw new IllegalArgumentException("Invalid operation type: " + operationType);
        }
    }


}