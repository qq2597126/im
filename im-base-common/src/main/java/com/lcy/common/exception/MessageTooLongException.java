package com.lcy.common.exception;

public class MessageTooLongException extends RuntimeException {
    public MessageTooLongException(String message) {
        super(message);
    }
}
