package com.dating.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DatingAppException extends RuntimeException {

    private final HttpStatus status;

    public DatingAppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public DatingAppException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static DatingAppException notFound(String resource) {
        return new DatingAppException(resource + " not found", HttpStatus.NOT_FOUND);
    }

    public static DatingAppException badRequest(String message) {
        return new DatingAppException(message, HttpStatus.BAD_REQUEST);
    }

    public static DatingAppException unauthorized(String message) {
        return new DatingAppException(message, HttpStatus.UNAUTHORIZED);
    }

    public static DatingAppException forbidden(String message) {
        return new DatingAppException(message, HttpStatus.FORBIDDEN);
    }

    public static DatingAppException conflict(String message) {
        return new DatingAppException(message, HttpStatus.CONFLICT);
    }
}
