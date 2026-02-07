package com.makanforyou.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for unauthorized access to chat resources
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChatAccessDeniedException extends RuntimeException {

    public ChatAccessDeniedException(Long conversationId, Long userId) {
        super("User " + userId + " does not have access to conversation " + conversationId);
    }

    public ChatAccessDeniedException(String message) {
        super(message);
    }
}
