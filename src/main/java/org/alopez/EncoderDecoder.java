package org.alopez;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EncoderDecoder {
    private static final int SERVICE_NUMBER_SIZE = 2;
    private static final int INFO_SIZE = 2;
    private static final Pattern OPERATION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)? [+-/*] \\d+(\\.\\d+)?$");
    private static final Pattern RESULT_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    public byte[] encodeOperation(String operation) {
        if (!OPERATION_PATTERN.matcher(operation).matches()) {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
        return encode(operation, getServiceNumber(operation));
    }

    public byte[] encodeResult(String result) {
        if (!RESULT_PATTERN.matcher(result).matches()) {
            throw new IllegalArgumentException("Invalid result: " + result);
        }
        return encode(result, (short) 99);
    }

    public String decode(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        short serviceNumber = buffer.getShort();
        short operationSize = buffer.getShort();
        byte[] operationBytes = new byte[operationSize];
        buffer.get(operationBytes);
        return serviceNumber + "|" + new String(operationBytes, StandardCharsets.UTF_8);
    }

    public short getOperationCode(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        return buffer.getShort();
    }

    private byte[] encode(String information, short serviceNumber) {
        byte[] informationBytes = information.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(SERVICE_NUMBER_SIZE + INFO_SIZE + informationBytes.length);
        buffer.putShort(serviceNumber);
        buffer.putShort((short) informationBytes.length);
        buffer.put(informationBytes);
        return buffer.array();
    }

    public String extractOperation(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.getShort();  // ignore the serviceNumber
        short operationSize = buffer.getShort();
        byte[] operationBytes = new byte[operationSize];
        buffer.get(operationBytes);
        return new String(operationBytes, StandardCharsets.UTF_8);
    }


    private short getServiceNumber(String operation) {
        if (operation.contains("+")) {
            return 1;
        } else if (operation.contains("-")) {
            return 2;
        } else if (operation.contains("*")) {
            return 3;
        } else if (operation.contains("/")) {
            return 4;
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
}
