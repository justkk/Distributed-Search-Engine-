package edu.upenn.cis.cis455.exceptions;

public class IllegalResponseStateException extends RuntimeException {

    public IllegalResponseStateException() {
    }

    public IllegalResponseStateException(String message) {
        super(message);
    }

    public IllegalResponseStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalResponseStateException(Throwable cause) {
        super(cause);
    }

    public IllegalResponseStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
