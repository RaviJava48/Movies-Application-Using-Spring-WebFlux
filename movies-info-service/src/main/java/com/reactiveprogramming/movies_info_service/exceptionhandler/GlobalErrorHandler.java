package com.reactiveprogramming.movies_info_service.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleValidationError(WebExchangeBindException ex) {

        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
