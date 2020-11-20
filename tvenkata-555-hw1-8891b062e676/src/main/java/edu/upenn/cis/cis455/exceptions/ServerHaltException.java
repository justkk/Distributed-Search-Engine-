package edu.upenn.cis.cis455.exceptions;

/*
 ***
 * ServerHaltException is thrown when the server is unable to start.
 * Its a runtime exception.
 * it can be because of incorrect static folder or issues with binding.
 */

public class ServerHaltException extends RuntimeException {

    public ServerHaltException() {
    }

    public ServerHaltException(String message) {
        super(message);
    }

    public ServerHaltException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerHaltException(Throwable cause) {
        super(cause);
    }

    public ServerHaltException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
