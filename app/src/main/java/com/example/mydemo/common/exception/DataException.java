package com.example.mydemo.common.exception;

import androidx.annotation.Nullable;

public class DataException extends RuntimeException {

    public DataException(String message) {
        super(message);
    }

    public DataException(Throwable cause, String message) {
        super(message, cause);
    }

}
