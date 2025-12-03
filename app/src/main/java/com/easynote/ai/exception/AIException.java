package com.easynote.ai.exception;

/**
 * AI通用异常
 */
public class AIException extends Exception {
    /**
     * AI异常类
     */
    private final boolean retryable;

    public AIException(String message) {
        super(message);
        this.retryable = true; // 默认可重试
    }

    public AIException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = true;
    }

    public AIException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}