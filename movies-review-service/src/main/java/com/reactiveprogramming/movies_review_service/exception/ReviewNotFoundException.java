package com.reactiveprogramming.movies_review_service.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewNotFoundException extends RuntimeException{

    private String message;
    private Throwable ex;

    public ReviewNotFoundException() {
        super();
    }

    public ReviewNotFoundException(String message, Throwable ex) {
        super(message, ex);
        this.message = message;
        this.ex = ex;
    }

    public ReviewNotFoundException(String message) {
        super(message);
        this.message = message;
    }
}
