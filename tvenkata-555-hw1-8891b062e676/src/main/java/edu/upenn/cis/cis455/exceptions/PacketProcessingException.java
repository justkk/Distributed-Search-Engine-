package edu.upenn.cis.cis455.exceptions;

/*
    ***
    * PacketProcessingException is thrown when the server is unable to parse the request.
    * Its a runtime exception.
    * Socket will be closed with out any response.
 */

public class PacketProcessingException extends RuntimeException {

    public PacketProcessingException() {
    }

    public PacketProcessingException(String message) {
        super(message);
    }

    public PacketProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketProcessingException(Throwable cause) {
        super(cause);
    }

    public PacketProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
