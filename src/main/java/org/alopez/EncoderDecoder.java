package org.alopez;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EncoderDecoder {

    // Constants representing service numbers
    private static final short ADDITION_CODE = 1;
    private static final short SUBTRACTION_CODE = 2;
    private static final short MULTIPLICATION_CODE = 3;
    private static final short DIVISION_CODE = 4;
    private static final short RESULT_CODE = 5;
    private static final short ACKNOWLEDGEMENT_CODE = 99;

    // Fixed sizes for elements
    private static final int SERVICE_NUMBER_SIZE = 2;
    private static final int INFO_SIZE = 2;

    // Patterns for validation
    private static final Pattern OPERATION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)? [+-/*] \\d+(\\.\\d+)?$");
    private static final Pattern RESULT_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    public byte[] encodeOperation(String operation) {
        if (!OPERATION_PATTERN.matcher(operation).matches()) {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
        return encode(operation, getServiceCode(operation));
    }

    public byte[] encodeAcknowledgement(String acknowledgement) {
        return encode(acknowledgement, ACKNOWLEDGEMENT_CODE);
    }

    public byte[] encodeResult(String result) {
        if (!RESULT_PATTERN.matcher(result).matches()) {
            throw new IllegalArgumentException("Invalid result: " + result);
        }
        return encode(result, RESULT_CODE);
    }

    public String decode(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        short serviceCode = buffer.getShort();
        short operationSize = buffer.getShort();
        byte[] operationBytes = new byte[operationSize];
        buffer.get(operationBytes);
        return serviceCode + "|" + new String(operationBytes, StandardCharsets.UTF_8);
    }

    public short getOperationCode(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        return buffer.getShort();
    }

    private byte[] encode(String information, short serviceCode) {
        byte[] informationBytes = information.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(SERVICE_NUMBER_SIZE + INFO_SIZE + informationBytes.length);
        buffer.putShort(serviceCode);
        buffer.putShort((short) informationBytes.length);
        buffer.put(informationBytes);
        return buffer.array();
    }

    public String extractOperation(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        if (buffer.remaining() < SERVICE_NUMBER_SIZE + INFO_SIZE) {
            throw new IllegalArgumentException("Incomplete message: missing service number or operation size info");
        }
        buffer.getShort();  // ignore the serviceCode
        short operationSize = buffer.getShort();

        if (buffer.remaining() < operationSize) {
            throw new IllegalArgumentException("Incomplete message: expected " + operationSize + " bytes but got " + buffer.remaining());
        }

        byte[] operationBytes = new byte[operationSize];
        buffer.get(operationBytes);
        return new String(operationBytes, StandardCharsets.UTF_8);
    }


    // Return the corresponding service code based on the operation
    private short getServiceCode(String operation) {
        if (operation.contains("+")) {
            return ADDITION_CODE;
        } else if (operation.contains("-")) {
            return SUBTRACTION_CODE;
        } else if (operation.contains("*")) {
            return MULTIPLICATION_CODE;
        } else if (operation.contains("/")) {
            return DIVISION_CODE;
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
}
