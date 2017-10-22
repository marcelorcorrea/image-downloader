package com.marcelorcorrea.imagedownloader.core.exception;

import java.io.IOException;

/**
 * Created by marcelo on 11/1/16.
 */
public class UnknownContentTypeException extends IOException {

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
