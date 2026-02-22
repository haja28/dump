package com.makanforyou.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for message not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(Long messageId) {
        super("Message not found with id: " + messageId);
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}
