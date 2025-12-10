package com.togglecover.common.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}