package com.reactiveprogramming.movies_review_service.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDataException extends RuntimeException {

    private String message;

    public ReviewDataException() {
        super();
    }

    public ReviewDataException(String message) {
        super(message);
        this.message=message;
    }
}
