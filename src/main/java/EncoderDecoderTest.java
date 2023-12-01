import org.alopez.EncoderDecoder;

import java.util.Arrays;
import java.util.UUID;

public class EncoderDecoderTest {

    public static void main(String[] args) {
        EncoderDecoder encoderDecoder = new EncoderDecoder();

        // Test encoding and decoding an arithmetic operation
        String operation = "6 * 6";
        String event = UUID.randomUUID().toString();
        byte[] encodedOperation = encoderDecoder.encodeArithmeticOperation(operation, event);
        System.out.println("Encoded operation: " + Arrays.toString(encodedOperation));
        String decodedOperation = encoderDecoder.decode(encodedOperation);
        System.out.println("Decoded operation: " + decodedOperation);
        String operationString = EncoderDecoder.extractArithmeticOperation(encodedOperation);
        System.out.println("Extracted operation: " + operationString);

        // Test encoding and decoding an acknowledgement
        String acknowledgementEvent = "def";
        byte[] encodedAcknowledgement = encoderDecoder.encodeAcknowledgement(acknowledgementEvent);
        System.out.println("Encoded acknowledgement: " + Arrays.toString(encodedAcknowledgement));
        String decodedAcknowledgement = encoderDecoder.decode(encodedAcknowledgement);
        System.out.println("Decoded acknowledgement: " + decodedAcknowledgement);

        // Test encoding and decoding a result
        String resultEvent = "ghi";
        int result = 7;
        byte[] encodedResult = encoderDecoder.encodeResult(resultEvent, result);
        System.out.println("Encoded result: " + Arrays.toString(encodedResult));
        String decodedResult = encoderDecoder.decode(encodedResult);
        System.out.println("Decoded result: " + decodedResult);
    }
}
