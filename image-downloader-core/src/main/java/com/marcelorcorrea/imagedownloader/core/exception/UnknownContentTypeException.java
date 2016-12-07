package com.marcelorcorrea.imagedownloader.core.exception;

/**
 * Created by marcelo on 11/1/16.
 */
public class UnknownContentTypeException extends Exception {

    public UnknownContentTypeException(String message) {
        super(message);
    }

    public UnknownContentTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownContentTypeException(Throwable cause) {
        super(cause);
    }
}
