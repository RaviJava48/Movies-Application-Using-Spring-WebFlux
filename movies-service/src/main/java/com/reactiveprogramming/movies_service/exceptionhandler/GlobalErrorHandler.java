package com.reactiveprogramming.movies_service.exceptionhandler;

import com.reactiveprogramming.movies_service.exception.MoviesInfoClientException;
import com.reactiveprogramming.movies_service.exception.MoviesInfoServerException;
import com.reactiveprogramming.movies_service.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleMoviesInfoClientError(MoviesInfoClientException ex) {

        log.error("Exception caught in handleClientError(): {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }

    @ExceptionHandler(MoviesInfoServerException.class)
    public ResponseEntity<String> handleMoviesInfoServerError(MoviesInfoServerException ex) {

        log.error("Exception caught in handleMoviesInfoServerError(): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewsServerException.class)
    public ResponseEntity<String> handleReviewsServerError(ReviewsServerException ex) {

        log.error("Exception caught in handleReviewsServerError(): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
